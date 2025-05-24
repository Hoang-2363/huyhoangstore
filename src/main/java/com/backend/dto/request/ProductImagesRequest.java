package com.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductImagesRequest {

    @Size(max = 1000, message = "Đường dẫn hình ảnh tối đa 1000 ký tự")
    private String imageUrl;
}
