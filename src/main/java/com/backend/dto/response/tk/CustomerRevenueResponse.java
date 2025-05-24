package com.backend.dto.response.tk;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerRevenueResponse {
    private String email;
    private String name;
    private String phone;
    private BigDecimal totalAmount;

    public CustomerRevenueResponse(String email, String name, String phone, BigDecimal totalAmount) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.totalAmount = totalAmount;
    }
}
