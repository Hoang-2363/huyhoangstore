package com.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 255, message = "Tên danh mục tối đa 255 ký tự")
    private String name;

    @Size(max = 1000, message = "Đường dẫn ảnh tối đa 1000 ký tự")
    private String imageUrl;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;
}
