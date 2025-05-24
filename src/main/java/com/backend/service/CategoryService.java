package com.backend.service;

import com.backend.dto.request.CategoryRequest;
import com.backend.model.Category;
import com.backend.model.Role;
import com.backend.repository.CategoryRepository;
import com.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private TokenService tokenService;

    public List<Category> getAllCategories(String token) {
        tokenService.validateRole(token, Role.ADMIN.name());
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public Category getCategoryById(String token, Long id) {
        tokenService.validateRole(token, Role.ADMIN.name());
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id = " + id));
    }

    public List<Category> searchCategories(String token, String searchContent) {
        tokenService.validateRole(token, Role.ADMIN.name());
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .filter(category -> (category.getName() != null && category.getName().toLowerCase().contains(searchContent.toLowerCase())) ||
                        (category.getDescription() != null && category.getDescription().toLowerCase().contains(searchContent.toLowerCase())))
                .collect(Collectors.toList());
    }

    public Category createCategory(String token, CategoryRequest request, MultipartFile file) throws IOException {
        tokenService.validateRole(token, Role.ADMIN.name());

        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Tên danh mục '" + request.getName() + "' đã tồn tại");
        }

        Category category = new Category();
        if (file != null && !file.isEmpty()) {
            imageService.deleteImage(request.getImageUrl());
            String newImageUrl = imageService.uploadImage(file, "categories");
            category.setImageUrl(newImageUrl);
        } else {
            category.setImageUrl(request.getImageUrl());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return categoryRepository.save(category);
    }

    public Category updateCategory(String token, Long id, CategoryRequest request, MultipartFile file) throws IOException {
        tokenService.validateRole(token, Role.ADMIN.name());
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id = " + id));

        if (file != null && !file.isEmpty()) {
            imageService.deleteImage(category.getImageUrl());
            String newImageUrl = imageService.uploadImage(file, "categories");
            category.setImageUrl(newImageUrl);
        } else {
            category.setImageUrl(category.getImageUrl());
        }

        if (!category.getName().equals(request.getName()) && categoryRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Tên danh mục '" + request.getName() + "' đã tồn tại");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return categoryRepository.save(category);
    }

    public void deleteCategory(String token, Long id) {
        tokenService.validateRole(token, Role.ADMIN.name());
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id = " + id));

        boolean isUsed = productRepository.existsByCategoriesContaining(category);
        if (isUsed) {
            throw new RuntimeException("Không thể xóa danh mục vì đang được sử dụng bởi sản phẩm.");
        }
        imageService.deleteImage(category.getImageUrl());
        categoryRepository.deleteById(id);
    }
}