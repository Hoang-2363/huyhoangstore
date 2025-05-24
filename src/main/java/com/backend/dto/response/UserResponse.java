package com.backend.dto.response;

import com.backend.model.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String gender;
    private String imgUrl;
    private String password;
    private String address;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime tokenExpiryTime;
    private String token;
}
