package com.backend.dto.response.tk;

import lombok.Data;

@Data
public class OrderCountStatResponse {
    private String timeGroup;
    private long count;

    public OrderCountStatResponse(String timeGroup, long count) {
        this.timeGroup = timeGroup;
        this.count = count;
    }
}
