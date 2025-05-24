package com.backend.controller;

import com.backend.dto.request.CategoryRequest;
import com.backend.model.Category;
import com.backend.service.CategoryService;
import com.backend.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TokenService tokenService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories(@RequestHeader("Authorization") String token) {
        List<Category> categories = categoryService.getAllCategories(tokenService.cleanToken(token));
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Category category = categoryService.getCategoryById(tokenService.cleanToken(token), id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Category>> search(
            @RequestHeader("Authorization") String token,
            @RequestParam("q") String searchContent
    ) {
        List<Category> categories = categoryService.searchCategories(tokenService.cleanToken(token), searchContent);
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute CategoryRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        Category createdCategory = categoryService.createCategory(tokenService.cleanToken(token), request, file);
        return ResponseEntity.ok(createdCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @ModelAttribute CategoryRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        Category updatedCategory = categoryService.updateCategory(tokenService.cleanToken(token), id, request, file);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        categoryService.deleteCategory(tokenService.cleanToken(token), id);
        return ResponseEntity.noContent().build();
    }
}