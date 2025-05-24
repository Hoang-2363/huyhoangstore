package com.backend.dto.response.tk;

import lombok.Data;

@Data
public class OrderStatusCountResponse {
    private String status;
    private long count;

    public OrderStatusCountResponse(String status, long count) {
        this.status = status;
        this.count = count;
    }
}
