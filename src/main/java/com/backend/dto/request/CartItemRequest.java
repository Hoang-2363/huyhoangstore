package com.backend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CartItemRequest {
    private ProductSimpleRequest product;
    private int quantity;

    @Data
    public static class ProductSimpleRequest {
        private String name;
        private Double priceSelling;
        private List<String> imageUrls;
    }
}
