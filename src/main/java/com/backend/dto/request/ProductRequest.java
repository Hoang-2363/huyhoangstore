package com.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductRequest {
    private String productCode;

    @NotBlank(message = "Tên sản phẩm không để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 1, message = "Số lượng tồn kho không được nhỏ hơn 0")
    private Integer stockQuantity;

    @Positive(message = "Giá nhập phải là số dương")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá nhập phải là số dương")
    private BigDecimal priceImport;

    @Positive(message = "Giá bán phải là số dương")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá bán phải là số dương")
    private BigDecimal priceSelling;

    // Loại dây
    @NotBlank(message = "Loại dây không được để trống")
    @Size(max = 255, message = "Loại dây không được vượt quá 255 ký tự")
    private String strapType;

    // Loại máy
    @NotBlank(message = "Loại máy không được để trống")
    @Size(max = 255, message = "Loại máy không được vượt quá 255 ký tự")
    private String movementType;

    // Kích thước mặt
    @NotBlank(message = "Kích thước mặt không được để trống")
    @Size(max = 255, message = "Kích thước mặt không được vượt quá 255 ký tự")
    private String caseSize;

    // Độ dày mặt
    @NotBlank(message = "Độ dày mặt không được để trống")
    @Size(max = 255, message = "Độ dày mặt không được vượt quá 255 ký tự")
    private String thickness;

    // Chất liệu mặt kính
    @NotBlank(message = "Chất liệu mặt kính không được để trống")
    @Size(max = 255, message = "Chất liệu mặt kính không được vượt quá 255 ký tự")
    private String glassMaterial;

    // Chất liệu vỏ
    @NotBlank(message = "Chất liệu vỏ không được để trống")
    @Size(max = 255, message = "Chất liệu vỏ không được vượt quá 255 ký tự")
    private String caseMaterial;

    // Khả năng kháng nước
    @NotBlank(message = "Khả năng kháng nước không được để trống")
    @Size(max = 255, message = "Khả năng kháng nước không được vượt quá 255 ký tự")
    private String waterResistance;

    // Bảo hành
    @NotBlank(message = "Thông tin bảo hành không được để trống")
    @Size(max = 255, message = "Thông tin bảo hành không được vượt quá 255 ký tự")
    private String warranty;

    @NotNull(message = "ID thương hiệu không được để trống")
    private Long brandId;

    @NotEmpty(message = "Danh sách danh mục không được để trống")
    private Set<Long> categoryIds;
}
