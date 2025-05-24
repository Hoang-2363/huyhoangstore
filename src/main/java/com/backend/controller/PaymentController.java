package com.backend.controller;

import com.backend.dto.request.PaymentRequest;
import com.backend.dto.response.PaymentResponse;
import com.backend.model.Payment;
import com.backend.service.PaymentService;
import com.backend.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/check-payment")
public class    PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    TokenService tokenService;

    @GetMapping
    public List<Map<String, Object>> getAllPayments(@RequestHeader("Authorization") String token) {
        return paymentService.getAllPayments(tokenService.cleanToken(token));
    }

    @GetMapping("/{id}")
    public Map<String, Object> getPaymentById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        return paymentService.getPaymentById(id, tokenService.cleanToken(token));
    }

    @PutMapping("/status/{id}")
    public Map<String, Object> updatePaymentStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody PaymentRequest paymentRequest) {
        return paymentService.updatePaymentStatus(id, paymentRequest, tokenService.cleanToken(token));
    }

    @GetMapping("/online/{orderCode}")
    public ResponseEntity<?> checkPayment(@PathVariable String orderCode) {
        try {
            boolean isPaid = paymentService.checkPaymentStatus(orderCode);
            if (isPaid) {
                return ResponseEntity.ok(new PaymentResponse(true, "Payment confirmed"));
            }
            return ResponseEntity.ok(new PaymentResponse(false, "Payment not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new PaymentResponse(false, "Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/cod/{orderCode}")
    public ResponseEntity<?> createPaymentCOD(@PathVariable String orderCode) {
        try {
            paymentService.createPaymentCOD(orderCode);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Tạo thông tin thanh toán COD thành công cho đơn hàng: " + orderCode);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }
}