package com.backend.service;

import com.backend.dto.request.BrandRequest;
import com.backend.model.Brand;
import com.backend.model.Role;
import com.backend.repository.BrandRepository;
import com.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private TokenService tokenService;

    public List<Brand> getAllBrands(String token) {
        tokenService.validateRole(token, Role.ADMIN.name());
        return brandRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public Brand getBrandById(String token, Long id) {
        tokenService.validateRole(token, Role.ADMIN.name());
        return brandRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với id = " + id));
    }

    public List<Brand> searchBrands(String token, String searchContent) {
        tokenService.validateRole(token, Role.ADMIN.name());
        return brandRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .filter(brand -> brand.getName() != null && brand.getName().toLowerCase().contains(searchContent.toLowerCase()) ||
                        brand.getDescription() != null && brand.getDescription().toLowerCase().contains(searchContent.toLowerCase()) ||
                        brand.getCountry() != null && brand.getCountry().toLowerCase().contains(searchContent.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Brand createBrand(String token, BrandRequest request, MultipartFile file) throws IOException {
        tokenService.validateRole(token, Role.ADMIN.name());

        if (brandRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Tên thương hiệu '" + request.getName() + "' đã tồn tại");
        }

        Brand brand = new Brand();
        if (file != null && !file.isEmpty()) {
            imageService.deleteImage(request.getImageUrl());
            String newImageUrl = imageService.uploadImage(file, "brands");
            brand.setImageUrl(newImageUrl);
        } else {
            brand.setImageUrl(request.getImageUrl());
        }

        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setCountry(request.getCountry());

        return brandRepository.save(brand);
    }

    public Brand updateBrand(String token, Long id, BrandRequest request, MultipartFile file) throws IOException {
        tokenService.validateRole(token, Role.ADMIN.name());

        Brand brand = brandRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với id = " + id));

        if (file != null && !file.isEmpty()) {
            imageService.deleteImage(brand.getImageUrl());
            String newImageUrl = imageService.uploadImage(file, "brands");
            brand.setImageUrl(newImageUrl);
        } else {
            brand.setImageUrl(brand.getImageUrl());
        }

        if (!brand.getName().equals(request.getName()) && brandRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Tên thương hiệu '" + request.getName() + "' đã tồn tại");
        }

        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setCountry(request.getCountry());
        brand.setUpdatedAt(LocalDateTime.now());

        return brandRepository.save(brand);
    }

    public void deleteBrand(String token, Long id) {
        tokenService.validateRole(token, Role.ADMIN.name());

        Brand brand = brandRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với id = " + id));

        boolean isUsed = productRepository.existsByBrand(brand);
        if (isUsed) {
            throw new RuntimeException("Không thể xóa thương hiệu vì đang được sử dụng bởi sản phẩm.");
        }
        imageService.deleteImage(brand.getImageUrl());
        brandRepository.deleteById(id);
    }
}
