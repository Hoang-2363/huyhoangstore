package com.backend.dto.request;

import lombok.Data;

@Data
public class ConfirmOrderRequest {
    private String orderCode;
    private String paymentMethod;
}
