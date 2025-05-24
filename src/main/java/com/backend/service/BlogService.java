package com.backend.service;

import com.backend.dto.request.BlogRequest;
import com.backend.model.Blog;
import com.backend.model.Role;
import com.backend.repository.BlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private TokenService tokenService;

    public List<Blog> getAllBlogs() {
        return blogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Blog getBlogById(Long id) {
        return blogRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy blog với id = " + id));
    }

    public List<Blog> searchBlogs(String searchContent) {
        return blogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().filter(blog -> blog.getTitle() != null && blog.getTitle().toLowerCase().contains(searchContent.toLowerCase()) || blog.getContent() != null && blog.getContent().toLowerCase().contains(searchContent.toLowerCase())).collect(Collectors.toList());
    }

    public Blog createBlog(String token, BlogRequest request, MultipartFile file) throws IOException {
        tokenService.validateRole(token, Role.ADMIN.name());

        if (blogRepository.findByTitle(request.getTitle()).isPresent()) {
            throw new RuntimeException("Tên tiêu đề '" + request.getTitle() + "' đã tồn tại");
        }

        Blog blog = new Blog();
        if (file != null && !file.isEmpty()) {
            String newImageUrl = imageService.uploadImage(file, "blogs");
            blog.setImageUrl(newImageUrl);
        } else {
            blog.setImageUrl(request.getImageUrl());
        }

        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setIsPublished(request.getIsPublished() != null ? request.getIsPublished() : false);

        return blogRepository.save(blog);
    }

    public Blog updateBlog(String token, Long id, BlogRequest request, MultipartFile file) throws IOException {
        tokenService.validateRole(token, Role.ADMIN.name());

        Blog blog = blogRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy blog với id = " + id));

        if (file != null && !file.isEmpty()) {
            imageService.deleteImage(blog.getImageUrl());
            String newImageUrl = imageService.uploadImage(file, "blogs");
            blog.setImageUrl(newImageUrl);
        }

        if (!blog.getTitle().equals(request.getTitle()) && blogRepository.findByTitle(request.getTitle()).isPresent()) {
            throw new RuntimeException("Tên tiêu đề '" + request.getTitle() + "' đã tồn tại");
        }

        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setIsPublished(request.getIsPublished() != null ? request.getIsPublished() : false);
        blog.setUpdatedAt(LocalDateTime.now());

        return blogRepository.save(blog);
    }

    public void deleteBlog(String token, Long id) {
        tokenService.validateRole(token, Role.ADMIN.name());

        Blog blog = blogRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy blog với id = " + id));

        imageService.deleteImage(blog.getImageUrl());
        blogRepository.deleteById(id);
    }
}
