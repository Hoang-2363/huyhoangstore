package com.backend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String productCode;
    private String name;
    private String description;
    private Integer stockQuantity;
    private BigDecimal priceImport;
    private BigDecimal priceSelling;
    private String strapType;
    private String movementType;
    private String caseSize;
    private String thickness;
    private String glassMaterial;
    private String caseMaterial;
    private String waterResistance;
    private String warranty;
    private BrandResponse brand;
    private List<CategoryResponse> categories;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
