package com.backend.controller;

import com.backend.dto.request.BatchProductRequest;
import com.backend.dto.request.ProductFilterRequest;
import com.backend.dto.request.ProductRequest;
import com.backend.dto.response.ProductResponse;
import com.backend.model.Brand;
import com.backend.model.Category;
import com.backend.service.ProductService;
import com.backend.service.TokenService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private TokenService tokenService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "brandId", required = false) Long brandId) {

        List<ProductResponse> products = productService.getAllProducts(sortBy, minPrice, maxPrice, categoryId, brandId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/brands")
    public ResponseEntity<List<Brand>> getAllBrands() {
        List<Brand> brands = productService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ProductResponse>> getProductsByIds(@RequestBody BatchProductRequest request) {
        List<ProductResponse> products = productService.getProductsByIds(request.getIds());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(
            @RequestParam("q") String searchContent
    ) {
        List<ProductResponse> productList = productService.searchProducts(searchContent);
        return ResponseEntity.ok(productList);
    }

    @PostMapping("/filter")
    public ResponseEntity<List<ProductResponse>> filterProducts(@RequestBody ProductFilterRequest filterRequest) {
        List<ProductResponse> filteredProducts = productService.filterProducts(filterRequest);
        return ResponseEntity.ok(filteredProducts);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute ProductRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
        ProductResponse createdProduct = productService.createProduct(tokenService.cleanToken(token), request, files);
        return ResponseEntity.ok(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @ModelAttribute ProductRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "removedImageUrls", required = false) String removedImageUrlsJson
    ) throws IOException {
        List<String> removedImageUrls = new ArrayList<>();
        if (removedImageUrlsJson != null) {
            removedImageUrls = new ObjectMapper().readValue(removedImageUrlsJson, new TypeReference<>() {});
        }
        ProductResponse updatedProduct = productService.updateProduct(tokenService.cleanToken(token), id, request, files, removedImageUrls);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id
    ) {
        productService.deleteProduct(tokenService.cleanToken(token), id);
        return ResponseEntity.noContent().build();
    }
}
