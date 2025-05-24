package com.backend.dto.response;

import com.backend.model.Order;
import com.backend.model.Payment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderWithPaymentResponse {
    private Long orderId;
    private String orderCode;
    private LocalDateTime orderDate;
    private BigDecimal totalCost;
    private String orderStatus;

    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private String transactionId;

    private List<OrderItemResponse> items;

    public OrderWithPaymentResponse(Order order, Payment payment,  List<OrderItemResponse> items) {
        this.orderId = order.getId();
        this.orderCode = order.getOrderCode();
        this.orderDate = order.getOrderDate();
        this.totalCost = order.getTotalCost();
        this.orderStatus = order.getStatus();

        if (payment != null) {
            this.paymentMethod = payment.getPaymentMethod();
            this.paymentStatus = payment.getStatus();
            this.paymentDate = payment.getPaymentDate();
            this.transactionId = payment.getTransactionId();
        }
        this.items = items;
    }
}
