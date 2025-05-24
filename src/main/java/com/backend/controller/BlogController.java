package com.backend.controller;

import com.backend.dto.request.BlogRequest;
import com.backend.model.Blog;
import com.backend.service.BlogService;
import com.backend.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private TokenService tokenService;

    @GetMapping
    public ResponseEntity<List<Blog>> getAllBlogs() {
        List<Blog> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Blog> getBlogById(@PathVariable Long id) {
        Blog blog = blogService.getBlogById(id);
        return ResponseEntity.ok(blog);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Blog>> searchBlogs(
            @RequestParam("q") String searchContent
    ) {
        List<Blog> blogs = blogService.searchBlogs(searchContent);
        return ResponseEntity.ok(blogs);
    }

    @PostMapping
    public ResponseEntity<Blog> createBlog(
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute BlogRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        Blog createdBlog = blogService.createBlog(tokenService.cleanToken(token), request, file);
        return ResponseEntity.ok(createdBlog);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Blog> updateBlog(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @ModelAttribute BlogRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        Blog updatedBlog = blogService.updateBlog(tokenService.cleanToken(token), id, request, file);
        return ResponseEntity.ok(updatedBlog);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id
    ) {
        blogService.deleteBlog(tokenService.cleanToken(token), id);
        return ResponseEntity.noContent().build();
    }
}
