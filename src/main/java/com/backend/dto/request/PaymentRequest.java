package com.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotBlank(message = "Mã đơn hàng không được để trống")
    private String orderCode;

    @Size(max = 100, message = "Phương thức thanh toán không được vượt quá 100 ký tự")
    private String paymentMethod;

    @Size(max = 100, message = "Trạng thái thanh toán không được vượt quá 100 ký tự")
    private String status;

    @Size(max = 255, message = "Mã giao dịch không được vượt quá 255 ký tự")
    private String transactionId;
}
