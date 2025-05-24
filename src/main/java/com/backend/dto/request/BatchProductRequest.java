package com.backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class BatchProductRequest {
    private List<Long> ids;
}