package com.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandRequest {

    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 255, message = "Tên thương hiệu tối đa 255 ký tự")
    private String name;

    @Size(max = 1000, message = "Đường dẫn ảnh tối đa 1000 ký tự")
    private String imageUrl;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @Size(max = 255, message = "Tên quốc gia tối đa 255 ký tự")
    @NotBlank(message = "Tên quốc gia không được để trống")
    private String country;
}