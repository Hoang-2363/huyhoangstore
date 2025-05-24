package com.backend.controller;

import com.backend.dto.request.OrderRequest;
import com.backend.dto.response.*;
import com.backend.dto.response.tk.*;
import com.backend.service.OrderService;
import com.backend.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        OrderResponse orderResponse = orderService.createOrder(orderRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @PutMapping("/update-status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@RequestHeader("Authorization") String token,
                                                           @RequestBody OrderRequest orderRequest) {

        OrderResponse orderResponse = orderService.updateOrderStatus(tokenService.cleanToken(token), orderRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(@RequestHeader("Authorization") String token) {
        List<OrderResponse> orders = orderService.getAllOrders(tokenService.cleanToken(token));
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderResponse> getOrderByCode(@RequestHeader("Authorization") String token,
                                                        @PathVariable String orderCode) {
        OrderResponse orderResponse = orderService.getOrderByCode(tokenService.cleanToken(token), orderCode);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/order-payment-user")
    public ResponseEntity<List<OrderWithPaymentResponse>> getOrderWithPaymentResponse(@RequestHeader("Authorization") String token) {
        List<OrderWithPaymentResponse> orders = orderService.getOrderPaymentByUser(tokenService.cleanToken(token));
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/stats/revenue")
    public ResponseEntity<List<RevenueStatResponse>> getRevenueStats(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "month") String groupBy) {
        List<RevenueStatResponse> stats = orderService.getRevenueStats(tokenService.cleanToken(token), groupBy);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/revenue-by-customer")
    public ResponseEntity<List<CustomerRevenueResponse>> getRevenueByCustomer(
            @RequestHeader("Authorization") String token) {
        List<CustomerRevenueResponse> stats = orderService.getRevenueByCustomer(tokenService.cleanToken(token));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/order-status")
    public ResponseEntity<List<OrderStatusCountResponse>> getOrderStatusStats(@RequestHeader("Authorization") String token) {
        List<OrderStatusCountResponse> stats = orderService.getOrderCountByStatus(tokenService.cleanToken(token));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/top-products")
    public ResponseEntity<List<TopProductSoldResponse>> getTop10Products(@RequestHeader("Authorization") String token) {
        List<TopProductSoldResponse> topProducts = orderService.getTop10ProductSold(tokenService.cleanToken(token));
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/stats/order-count")
    public ResponseEntity<List<OrderCountStatResponse>> getOrderCountStats(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "month") String groupBy) {
        return ResponseEntity.ok(orderService.getOrderCountStats(tokenService.cleanToken(token), groupBy));
    }

    @GetMapping("/stats/total-product-sold")
    public ResponseEntity<TopProductSoldResponse> getTotalProductSold(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(orderService.getTotalProductSold(tokenService.cleanToken(token)));
    }

}
