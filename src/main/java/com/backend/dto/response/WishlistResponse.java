package com.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WishlistResponse {

    private Long id;
    private LocalDateTime createdAt;

    private UserResponse user;
    private ProductResponse product;
}
