package com.backend.dto.response.tk;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RevenueStatResponse {
    private String timeGroup;
    private BigDecimal totalAmount;

    public RevenueStatResponse(String timeGroup, BigDecimal totalAmount) {
        this.timeGroup = timeGroup;
        this.totalAmount = totalAmount;
    }
}
