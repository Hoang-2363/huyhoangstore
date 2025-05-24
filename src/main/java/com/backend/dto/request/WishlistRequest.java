package com.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WishlistRequest {

    @NotNull(message = "ID người dùng không được để trống")
    private Long userId;

    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;
}
