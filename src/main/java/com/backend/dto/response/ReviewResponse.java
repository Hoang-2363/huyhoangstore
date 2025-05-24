package com.backend.dto.response;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    private ProductResponse product;
    private UserResponse user;
}
