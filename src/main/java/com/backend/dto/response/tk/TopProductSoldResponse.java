package com.backend.dto.response.tk;

import lombok.Data;

@Data
public class TopProductSoldResponse {
    private String productCode;
    private String productName;
    private int totalSold;
    private int totalQuantity;

    public TopProductSoldResponse(String productCode, String productName, int totalSold) {
        this.productCode = productCode;
        this.productName = productName;
        this.totalSold = totalSold;
    }

    public TopProductSoldResponse(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
