package com.backend.service;

import com.backend.dto.request.PaymentRequest;
import com.backend.dto.response.OrderItemResponse;
import com.backend.dto.response.OrderWithPaymentResponse;
import com.backend.dto.response.ProductResponse;
import com.backend.model.Order;
import com.backend.model.OrderDetail;
import com.backend.model.Payment;
import com.backend.model.Role;
import com.backend.repository.OrderRepository;
import com.backend.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {
    @Value("${sepay.api.url}")
    private String sepayApiUrl;

    @Value("${sepay.api.key}")
    private String sepayApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    public PaymentService(RestTemplate restTemplate, ObjectMapper objectMapper, PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    private Map<String, Object> mapPaymentToMap(Payment payment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", payment.getId());
        map.put("orderId", payment.getOrder() != null ? payment.getOrder().getId() : null);
        map.put("paymentMethod", payment.getPaymentMethod());
        map.put("status", payment.getStatus());
        map.put("paymentDate", payment.getPaymentDate());
        map.put("transactionId", payment.getTransactionId());
        return map;
    }

    public List<Map<String, Object>> getAllPayments(String token) {
        tokenService.validateRole(token, Role.ADMIN.name());
        return paymentRepository.findAll().stream()
                .map(this::mapPaymentToMap)
                .toList();
    }

    public Map<String, Object> getPaymentById(Long id, String token) {
        tokenService.validateRole(token, Role.ADMIN.name());
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán với ID: " + id));
        return mapPaymentToMap(payment);
    }

    public Map<String, Object> updatePaymentStatus(Long id, PaymentRequest paymentRequest, String token) {
        tokenService.validateRole(token, Role.ADMIN.name());
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán với ID: " + id));

        String newStatus = paymentRequest.getStatus();
        payment.setStatus(newStatus);

        if ("Đã thanh toán".equalsIgnoreCase(newStatus)) {
            payment.setPaymentDate(LocalDateTime.now());
            Random random = new Random();
            String transactionId = "COD" + (1000000000L + (long)(random.nextDouble() * 9000000000L));
            payment.setTransactionId(transactionId);
        } else {
            payment.setPaymentDate(null);
            payment.setTransactionId(null);
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return mapPaymentToMap(updatedPayment);
    }

    public boolean checkPaymentStatus(String orderCode) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + sepayApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(sepayApiUrl, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.path("status").asInt() == 200) {
                JsonNode transactions = root.path("transactions");
                for (JsonNode tx : transactions) {
                    String content = tx.path("transaction_content").asText();
                    String transactionId = tx.path("reference_number").asText();
                    if (content.contains(orderCode)) {
                        Order order = orderRepository.findByOrderCode(orderCode).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã: " + orderCode));

                        Payment payment = new Payment();
                        payment.setOrder(order);
                        payment.setPaymentMethod("Thanh toán Online");
                        payment.setStatus("Đã thanh toán");
                        payment.setTransactionId(transactionId);
                        payment.setPaymentDate(LocalDateTime.now());

                        paymentRepository.save(payment);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void createPaymentCOD(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã: " + orderCode));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod("Thanh toán khi nhận hàng");
        payment.setStatus("Chưa thanh toán");
        payment.setTransactionId(null);
        payment.setPaymentDate(null);

        paymentRepository.save(payment);
    }
}
