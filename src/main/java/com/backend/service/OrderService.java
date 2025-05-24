package com.backend.service;

import com.backend.dto.request.OrderDetailRequest;
import com.backend.dto.request.OrderRequest;
import com.backend.dto.response.*;
import com.backend.dto.response.tk.*;
import com.backend.model.*;
import com.backend.repository.*;
import com.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TokenService tokenService;

    public OrderResponse createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setOrderDate(LocalDateTime.now());
        order.setTotalCost(orderRequest.getTotalCost());
        order.setStatus("Đang xác nhận");

        Optional<User> optionalUser = userRepository.findByEmail(orderRequest.getEmailUser());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            order.setUser(user);
            order.setNameUser(user.getName());
            order.setEmailUser(user.getEmail());
            order.setPhoneUser(user.getPhone());
            order.setImgUrlUser(user.getImgUrl());
            order.setAddressUser(user.getAddress());
        } else {
            if (!tokenService.checkEmailExistsOnGoogle(orderRequest.getEmailUser())) {
                throw new RuntimeException("Email không tồn tại trên Google");
            }
            order.setNameUser(orderRequest.getNameUser());
            order.setEmailUser(orderRequest.getEmailUser());
            order.setPhoneUser(orderRequest.getPhoneUser());
            order.setImgUrlUser(orderRequest.getImgUrlUser());
            order.setAddressUser(orderRequest.getAddressUser());
        }

        orderRepository.save(order);

        List<OrderDetail> orderDetailsList = new ArrayList<>();
        for (OrderDetailRequest itemRequest : orderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + itemRequest.getProductId()));

            OrderDetail orderDetail = getOrderDetail(itemRequest, order, product);

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            orderDetailsList.add(orderDetail);
        }

        orderDetailRepository.saveAll(orderDetailsList);

        order.setOrderDetails(orderDetailsList);
        orderRepository.save(order);

        return convertToOrderResponse(order);
    }

    public OrderResponse updateOrderStatus(String token, OrderRequest orderRequest) {
        tokenService.validateRole(token, Role.ADMIN.name());

        Order order = orderRepository.findByOrderCode(orderRequest.getOrderCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderRequest.getOrderCode()));

        order.setStatus(orderRequest.getStatus());
        orderRepository.save(order);

        return convertToOrderResponse(order);
    }

    public List<OrderResponse> getAllOrders(String token) {
        tokenService.validateRole(token, Role.ADMIN.name());
        return orderRepository.findAll().stream().map(this::convertToOrderResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> searchOrders(String token, String searchContent) {
        tokenService.validateRole(token, Role.ADMIN.name());

        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(order ->
                        (order.getOrderCode() != null && order.getOrderCode().toLowerCase().contains(searchContent.toLowerCase())) ||
                                (order.getNameUser() != null && order.getNameUser().toLowerCase().contains(searchContent.toLowerCase())) ||
                                (order.getPhoneUser() != null && order.getPhoneUser().toLowerCase().contains(searchContent.toLowerCase()))
                )
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderByCode(String token, String orderCode) {
        tokenService.validateRole(token, Role.ADMIN.name());
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderCode));

        return convertToOrderResponse(order);
    }

    public List<OrderWithPaymentResponse> getOrderPaymentByUser(String token) {
        String subject = jwtUtil.extractSubject(token);

        Optional<User> userByEmail = userRepository.findByEmail(subject);
        Optional<User> userByPhone = userRepository.findByPhone(subject);

        User user = userByEmail.orElseGet(() -> userByPhone
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!")));

        List<Order> orders = orderRepository.findByUser(user);
        List<OrderWithPaymentResponse> result = new ArrayList<>();
        for (Order order : orders) {
            Payment payment = paymentRepository.findByOrder(order).orElse(null);
            List<OrderItemResponse> orderItemResponses = new ArrayList<>();
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                OrderItemResponse itemResponse = new OrderItemResponse();

                ProductResponse productResponse = new ProductResponse();
                productResponse.setId(orderDetail.getProduct().getId());
                productResponse.setProductCode(orderDetail.getProductCode());
                productResponse.setName(orderDetail.getNameProduct());
                productResponse.setPriceSelling(orderDetail.getUnitPrice());
                productResponse.setImageUrls(List.of(orderDetail.getImageUrlProduct()));

                itemResponse.setProductResponse(productResponse);
                itemResponse.setQuantity(orderDetail.getQuantity());

                orderItemResponses.add(itemResponse);
            }
            result.add(new OrderWithPaymentResponse(order, payment, orderItemResponses));
        }
        return result;
    }

    //Thống kê
    public List<RevenueStatResponse> getRevenueStats(String token, String groupBy) {
        tokenService.validateRole(token, Role.ADMIN.name());

        List<Order> orders = orderRepository.findAll();

        Map<String, BigDecimal> revenueMap = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> {
                            if ("day".equalsIgnoreCase(groupBy)) {
                                return o.getOrderDate().toLocalDate().toString();
                            } else { // default: month
                                return YearMonth.from(o.getOrderDate()).toString();
                            }
                        },
                        Collectors.mapping(Order::getTotalCost, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return revenueMap.entrySet().stream()
                .map(entry -> new RevenueStatResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(RevenueStatResponse::getTimeGroup))
                .collect(Collectors.toList());
    }

    public List<CustomerRevenueResponse> getRevenueByCustomer(String token) {
        tokenService.validateRole(token, Role.ADMIN.name());

        List<Order> orders = orderRepository.findAll();

        Map<String, CustomerRevenueResponse> map = new HashMap<>();

        for (Order order : orders) {
            var user = order.getUser();
            String email = user.getEmail();
            String name = user.getName();
            String phone = user.getPhone();
            BigDecimal amount = order.getTotalCost();

            map.merge(email,
                    new CustomerRevenueResponse(email, name, phone, amount),
                    (oldVal, newVal) -> {
                        oldVal.setTotalAmount(oldVal.getTotalAmount().add(newVal.getTotalAmount()));
                        return oldVal;
                    });
        }

        return map.values().stream()
                .sorted(Comparator.comparing(CustomerRevenueResponse::getTotalAmount).reversed())
                .collect(Collectors.toList());
    }

    public List<TopProductSoldResponse> getTop10ProductSold(String token) {
        tokenService.validateRole(token, Role.ADMIN.name());

        List<Order> orders = orderRepository.findAll();

        Map<String, TopProductSoldResponse> productMap = new HashMap<>();

        for (Order order : orders) {
            for (OrderDetail item : order.getOrderDetails()) {
                var product = item.getProduct();
                String code = product.getProductCode();
                String name = product.getName();
                int quantity = item.getQuantity();

                productMap.merge(code,
                        new TopProductSoldResponse(code, name, quantity),
                        (oldVal, newVal) -> {
                            oldVal.setTotalSold(oldVal.getTotalSold() + newVal.getTotalSold());
                            return oldVal;
                        });
            }
        }

        return productMap.values().stream()
                .sorted(Comparator.comparingInt(TopProductSoldResponse::getTotalSold).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<OrderStatusCountResponse> getOrderCountByStatus(String token) {
        tokenService.validateRole(token, Role.ADMIN.name());

        List<Order> orders = orderRepository.findAll();

        Map<String, Long> statusMap = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getStatus,
                        Collectors.counting()
                ));

        return statusMap.entrySet().stream()
                .map(entry -> new OrderStatusCountResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(OrderStatusCountResponse::getStatus))
                .collect(Collectors.toList());
    }

    public List<OrderCountStatResponse> getOrderCountStats(String token, String groupBy) {
        tokenService.validateRole(token, Role.ADMIN.name());

        List<Order> orders = orderRepository.findAll();

        Map<String, Long> grouped = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> {
                            LocalDateTime date = order.getOrderDate();
                            switch (groupBy.toLowerCase()) {
                                case "day":
                                    return date.toLocalDate().toString(); // yyyy-MM-dd
                                case "week":
                                    WeekFields weekFields = WeekFields.ISO;
                                    int week = date.get(weekFields.weekOfWeekBasedYear());
                                    return date.getYear() + "-W" + String.format("%02d", week); // yyyy-Www
                                case "month":
                                default:
                                    return YearMonth.from(date).toString(); // yyyy-MM
                            }
                        },
                        Collectors.counting()
                ));

        return grouped.entrySet().stream()
                .map(e -> new OrderCountStatResponse(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(OrderCountStatResponse::getTimeGroup))
                .collect(Collectors.toList());
    }

    public TopProductSoldResponse getTotalProductSold(String token) {
        tokenService.validateRole(token, Role.ADMIN.name());

        int total = orderRepository.findAll().stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .mapToInt(OrderDetail::getQuantity)
                .sum();

        return new TopProductSoldResponse(total);
    }


    private OrderDetail getOrderDetail(OrderDetailRequest itemRequest, Order order, Product product) {
        BigDecimal unitPrice = itemRequest.getUnitPrice();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setProduct(product);
        orderDetail.setProductCode(itemRequest.getProductCode());
        orderDetail.setNameProduct(itemRequest.getNameProduct());
        orderDetail.setImageUrlProduct(itemRequest.getImageUrlProduct());
        orderDetail.setQuantity(itemRequest.getQuantity());
        orderDetail.setUnitPrice(unitPrice);
        orderDetail.setTotalPrice(totalPrice);
        return orderDetail;
    }

    private String generateOrderCode() {
        String prefix = "HDBH";
        long count = orderRepository.count();
        return prefix + String.format("%06d", count + 1);
    }

    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(order.getId());
        orderResponse.setOrderDate(order.getOrderDate());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setTotalAmount(order.getTotalCost().doubleValue());
        orderResponse.setTotalItems(order.getOrderDetails().size());
        orderResponse.setOrderCode(order.getOrderCode());

        OrderUserResponse orderUserResponse = new OrderUserResponse();
        orderUserResponse.setNameUser(order.getNameUser());
        orderUserResponse.setEmailUser(order.getEmailUser());
        orderUserResponse.setPhoneUser(order.getPhoneUser());
        orderUserResponse.setAddressUser(order.getAddressUser());
        orderResponse.setOrderUserResponse(orderUserResponse);

        List<OrderItemResponse> orderItemResponses = new ArrayList<>();
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            OrderItemResponse orderItemResponse = new OrderItemResponse();
            ProductResponse productResponse = new ProductResponse();
            productResponse.setId(orderDetail.getProduct().getId());
            productResponse.setProductCode(orderDetail.getProductCode());
            productResponse.setName(orderDetail.getNameProduct());
            productResponse.setPriceSelling(orderDetail.getUnitPrice());
            productResponse.setImageUrls(List.of(orderDetail.getImageUrlProduct()));

            orderItemResponse.setProductResponse(productResponse);
            orderItemResponse.setQuantity(orderDetail.getQuantity());
            orderItemResponses.add(orderItemResponse);
        }
        orderResponse.setItems(orderItemResponses);

        return orderResponse;
    }
}
