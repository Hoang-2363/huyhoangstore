package com.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    private String contact;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}