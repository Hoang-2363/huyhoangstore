package com.backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class BillRequest {
    private String emailUser;
    private String nameUser;
    private String phoneUser;
    private String addressUser;
    private List<CartItemRequest> cartItems;
    private int totalItems;
    private double totalAmount;
}
