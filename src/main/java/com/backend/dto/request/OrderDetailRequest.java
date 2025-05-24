package com.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailRequest {
    @NotNull(message = "ID đơn hàng không được để trống")
    private Long orderId;

    private Long productId;

    @NotBlank(message = "Mã sản phẩm không được để trống")
    private String productCode;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String nameProduct;

    @Size(max = 1000, message = "Đường dẫn ảnh không được vượt quá 1000 ký tự")
    private String imageUrlProduct;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Đơn giá phải là số dương")
    private BigDecimal unitPrice;

    @NotNull(message = "Tổng giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tổng giá phải là số dương")
    private BigDecimal totalPrice;
}
