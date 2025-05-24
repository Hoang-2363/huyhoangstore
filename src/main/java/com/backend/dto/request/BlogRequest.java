package com.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 60000, message = "Nội dung tối đa 3000 ký tự")
    private String content;

    @Size(max = 1000, message = "Đường dẫn ảnh tối đa 1000 ký tự")
    private String imageUrl;

    private Boolean isPublished;
}
