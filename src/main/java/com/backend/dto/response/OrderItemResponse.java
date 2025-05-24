package com.backend.dto.response;

import lombok.Data;

@Data
public class OrderItemResponse {
    private ProductResponse productResponse;
    private int quantity;
}
