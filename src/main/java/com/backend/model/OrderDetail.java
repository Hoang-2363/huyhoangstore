package com.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "order_details")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Product product;

    @Column(name="product_code", nullable = false)
    private String productCode;

    @Column(name="name_product", nullable = false)
    private String nameProduct;

    @Column(name = "image_url_product", length = 1000)
    private String imageUrlProduct;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;


    @Column(precision = 20, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 20, scale = 2, nullable = false)
    private BigDecimal totalPrice;
}
