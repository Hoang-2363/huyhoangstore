package com.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {

    @NotBlank(message = "Tên người đặt không được để trống")
    @Size(max = 50, message = "Tên người đặt không được vượt quá 50 ký tự")
    private String nameUser;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String emailUser;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(059|092|058|056|070|079|077|076|078|089|090|093|081|082|083|084|085|088|091|094|032|033|034|035|036|037|038|039|086|096|097|098)\\d{7}$",
            message = "Số điện thoại không hợp lệ!")
    private String phoneUser;

    @Size(max = 1000, message = "Đường dẫn ảnh không được vượt quá 1000 ký tự")
    private String imgUrlUser;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String addressUser;

    private String orderCode;

    @NotNull(message = "Tổng chi phí không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tổng chi phí phải là số dương")
    private BigDecimal totalCost;

    @Size(max = 255, message = "Trạng thái đơn hàng không được vượt quá 255 ký tự")
    private String status;

    private Long userId;

    @NotNull(message = "Danh sách chi tiết đơn hàng không được để trống")
    private List<OrderDetailRequest> items;
}
