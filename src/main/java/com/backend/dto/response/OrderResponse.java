package com.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private LocalDateTime orderDate;
    private String status;
    private OrderUserResponse orderUserResponse;
    private double totalAmount;
    private String orderCode;
    private int totalItems;
    private List<OrderItemResponse> items;
}
