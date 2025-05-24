package com.backend.controller;

import com.backend.dto.request.BrandRequest;
import com.backend.model.Brand;
import com.backend.service.BrandService;
import com.backend.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @Autowired
    TokenService tokenService;

    @GetMapping
    public ResponseEntity<List<Brand>> getAllBrands(@RequestHeader("Authorization") String token) {
        List<Brand> brands = brandService.getAllBrands(tokenService.cleanToken(token));
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Brand brands = brandService.getBrandById(tokenService.cleanToken(token), id);
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Brand>> search(
            @RequestHeader("Authorization") String token,
            @RequestParam("q") String searchContent
    ) {
        List<Brand> brandsList = brandService.searchBrands(tokenService.cleanToken(token), searchContent);
        return ResponseEntity.ok(brandsList);
    }

    @PostMapping
    public ResponseEntity<Brand> createBrand(
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute BrandRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        Brand createdBrand = brandService.createBrand(tokenService.cleanToken(token), request, file);
        return ResponseEntity.ok(createdBrand);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Brand> updateBrand(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @ModelAttribute BrandRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        Brand updatedBrand = brandService.updateBrand(tokenService.cleanToken(token), id, request, file);
        return ResponseEntity.ok(updatedBrand);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id
    ) {
        brandService.deleteBrand(tokenService.cleanToken(token), id);
        return ResponseEntity.noContent().build();
    }
}