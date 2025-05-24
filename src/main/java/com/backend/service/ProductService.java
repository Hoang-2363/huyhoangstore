package com.backend.service;

import com.backend.dto.request.ProductFilterRequest;
import com.backend.dto.request.ProductRequest;
import com.backend.dto.response.BrandResponse;
import com.backend.dto.response.CategoryResponse;
import com.backend.dto.response.ProductResponse;
import com.backend.model.*;
import com.backend.repository.BrandRepository;
import com.backend.repository.CategoryRepository;
import com.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<ProductResponse> getProductsByIds(List<Long> ids) {
        List<Product> products = productRepository.findByIdIn(ids);
        return products.stream().map(this::convertToProductResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> getAllProducts(String sortBy, BigDecimal minPrice, BigDecimal maxPrice, Long categoryId, Long brandId) {
        List<Product> products = productRepository.findAll();

        products = products.stream()
                .filter(p -> minPrice == null || p.getPriceSelling().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.getPriceSelling().compareTo(maxPrice) <= 0)
                .filter(p -> brandId == null || (p.getBrand() != null && p.getBrand().getId().equals(brandId)))
                .filter(p -> categoryId == null || (p.getCategories() != null &&
                        p.getCategories().stream().anyMatch(c -> c.getId().equals(categoryId))))
                .collect(Collectors.toList());

        Comparator<Product> comparator = getProductComparator(sortBy);

        if (comparator != null) {
            products.sort(comparator);
        }

        return products.stream().map(this::convertToProductResponse).collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id = " + id));

        return convertToProductResponse(product);
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<ProductResponse> searchProducts(String searchContent) {
        String keyword = searchContent.toLowerCase();

        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .filter(product ->
                        containsIgnoreCase(product.getProductCode(), keyword) ||
                                containsIgnoreCase(product.getName(), keyword) ||
                                containsIgnoreCase(product.getDescription(), keyword) ||
                                containsIgnoreCase(product.getStrapType(), keyword) ||
                                containsIgnoreCase(product.getMovementType(), keyword) ||
                                containsIgnoreCase(product.getCaseSize(), keyword) ||
                                containsIgnoreCase(product.getThickness(), keyword) ||
                                containsIgnoreCase(product.getGlassMaterial(), keyword) ||
                                containsIgnoreCase(product.getCaseMaterial(), keyword) ||
                                containsIgnoreCase(product.getWaterResistance(), keyword) ||
                                containsIgnoreCase(product.getWarranty(), keyword) ||
                                (product.getBrand() != null && containsIgnoreCase(product.getBrand().getName(), keyword)) ||
                                (product.getCategories() != null && product.getCategories().stream()
                                        .anyMatch(category -> containsIgnoreCase(category.getName(), keyword)))
                )
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> filterProducts(ProductFilterRequest filter) {
        List<Product> products = productRepository.findAll();

        Stream<Product> stream = products.stream();

        if (filter.getBrandsType() != null && !filter.getBrandsType().isEmpty()) {
            stream = stream.filter(p -> p.getBrand() != null && filter.getBrandsType().contains(p.getBrand().getName()));
        }

        if (filter.getCategoriesType() != null && !filter.getCategoriesType().isEmpty()) {
            stream = stream.filter(p -> p.getCategories() != null &&
                    p.getCategories().stream().anyMatch(c -> filter.getCategoriesType().contains(c.getName())));
        }

        if (filter.getPrices() != null && !filter.getPrices().isEmpty()) {
            stream = stream.filter(p -> {
                BigDecimal price = p.getPriceSelling();
                if (price == null) return false;

                for (String priceRangeLabel : filter.getPrices()) {
                    BigDecimal min = null;
                    BigDecimal max = null;

                    switch (priceRangeLabel) {
                        case "Dưới 1 triệu":
                            min = BigDecimal.ZERO;
                            max = new BigDecimal("1000000");
                            break;
                        case "Từ 1 - 5 triệu":
                            min = new BigDecimal("1000000");
                            max = new BigDecimal("5000000");
                            break;
                        case "Từ 5 - 10 triệu":
                            min = new BigDecimal("5000000");
                            max = new BigDecimal("10000000");
                            break;
                        case "Từ 10 - 20 triệu":
                            min = new BigDecimal("10000000");
                            max = new BigDecimal("20000000");
                            break;
                        case "Trên 20 triệu":
                            min = new BigDecimal("20000000");
                            max = null; // không giới hạn trên
                            break;
                        default:
                            continue;
                    }

                    if (max == null) {
                        if (price.compareTo(min) >= 0) return true;
                    } else {
                        if (price.compareTo(min) >= 0 && price.compareTo(max) <= 0) return true;
                    }
                }

                return false;
            });
        }

        List<String> genderCategories = new ArrayList<>();
        if (filter.getGenders() != null) {
            for (String gender : filter.getGenders()) {
                if ("Nam".equalsIgnoreCase(gender)) {
                    genderCategories.add("Đồng hồ nam");
                } else if ("Nữ".equalsIgnoreCase(gender)) {
                    genderCategories.add("Đồng hồ nữ");
                }
            }
        }
        if (!genderCategories.isEmpty()) {
            stream = stream.filter(p -> p.getCategories() != null &&
                    p.getCategories().stream().anyMatch(c -> genderCategories.contains(c.getName())));
        }

        // Lọc theo loại dây đeo
        if (filter.getStraps() != null && !filter.getStraps().isEmpty()) {
            stream = stream.filter(p -> p.getStrapType() != null && filter.getStraps().contains(p.getStrapType()));
        }

        // Lọc theo loại máy
        if (filter.getMovementTypes() != null && !filter.getMovementTypes().isEmpty()) {
            stream = stream.filter(p -> p.getMovementType() != null && filter.getMovementTypes().contains(p.getMovementType()));
        }

        // Lọc theo kích thước mặt
        if (filter.getCaseSizes() != null && !filter.getCaseSizes().isEmpty()) {
            stream = stream.filter(p -> p.getCaseSize() != null && filter.getCaseSizes().contains(p.getCaseSize()));
        }

        // Lọc theo độ dày
        if (filter.getThicknesses() != null && !filter.getThicknesses().isEmpty()) {
            stream = stream.filter(p -> p.getThickness() != null && filter.getThicknesses().contains(p.getThickness()));
        }

        // Lọc theo loại kính
        if (filter.getGlassMaterials() != null && !filter.getGlassMaterials().isEmpty()) {
            stream = stream.filter(p -> p.getGlassMaterial() != null && filter.getGlassMaterials().contains(p.getGlassMaterial()));
        }

        // Lọc theo chất liệu vỏ
        if (filter.getCaseMaterials() != null && !filter.getCaseMaterials().isEmpty()) {
            stream = stream.filter(p -> p.getCaseMaterial() != null && filter.getCaseMaterials().contains(p.getCaseMaterial()));
        }

        // Lọc theo mức độ chống nước
        if (filter.getWaterResistanceLevels() != null && !filter.getWaterResistanceLevels().isEmpty()) {
            stream = stream.filter(p -> p.getWaterResistance() != null && filter.getWaterResistanceLevels().contains(p.getWaterResistance()));
        }

        // Chuyển đổi và trả về kết quả
        return stream
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse createProduct(String token, ProductRequest request, List<MultipartFile> files) throws IOException {
        tokenService.validateRole(token, Role.ADMIN.name());

        if (productRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Tên sản phẩm '" + request.getName() + "' đã tồn tại!");
        }

        if (request.getPriceImport().compareTo(request.getPriceSelling()) >= 0) {
            throw new RuntimeException("Giá nhập phải nhỏ hơn giá bán!");
        }

        Product product = new Product();

        if (files != null && !files.isEmpty()) {
            for (ProductImages image : product.getImages()) {
                imageService.deleteImage(image.getImageUrl());
            }
            for (MultipartFile file : files) {
                String newImageUrl = imageService.uploadImage(file, "products");
                ProductImages productImage = new ProductImages();
                productImage.setImageUrl(newImageUrl);
                productImage.setProduct(product);
                product.getImages().add(productImage);
            }
        }

        if (product.getProductCode() == null || product.getProductCode().isEmpty()) {
            product.setProductCode(generateProductCode());
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setStockQuantity(request.getStockQuantity());
        product.setPriceImport(request.getPriceImport());
        product.setPriceSelling(request.getPriceSelling());
        product.setStrapType(request.getStrapType());
        product.setMovementType(request.getMovementType());
        product.setCaseSize(request.getCaseSize());
        product.setThickness(request.getThickness());
        product.setGlassMaterial(request.getGlassMaterial());
        product.setCaseMaterial(request.getCaseMaterial());
        product.setWaterResistance(request.getWaterResistance());
        product.setWarranty(request.getWarranty());

        Optional<Brand> brand = brandRepository.findById(request.getBrandId());
        if (brand.isEmpty()) {
            throw new RuntimeException("Thương hiệu không hợp lệ");
        }
        product.setBrand(brand.get());

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : request.getCategoryIds()) {
                Optional<Category> category = categoryRepository.findById(categoryId);
                if (category.isPresent()) {
                    categories.add(category.get());
                } else {
                    throw new RuntimeException("Danh mục với ID " + categoryId + " không tồn tại");
                }
            }
            product.setCategories(categories);
        }

        Product savedProduct = productRepository.save(product);

        return convertToProductResponse(savedProduct);
    }

    public ProductResponse updateProduct(String token, Long id, ProductRequest request, List<MultipartFile> files, List<String> removedImageUrls) throws IOException {
        tokenService.validateRole(token, Role.ADMIN.name());

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id = " + id));

        if (removedImageUrls != null && !removedImageUrls.isEmpty()) {
            List<ProductImages> toRemove = product.getImages().stream()
                    .filter(img -> removedImageUrls.contains(img.getImageUrl()))
                    .toList();

            for (ProductImages image : toRemove) {
                imageService.deleteImage(image.getImageUrl());
                product.getImages().remove(image);
            }
        }

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String newImageUrl = imageService.uploadImage(file, "products");
                ProductImages productImage = new ProductImages();
                productImage.setImageUrl(newImageUrl);
                productImage.setProduct(product);
                product.getImages().add(productImage);
            }
        }

        if (!product.getName().equals(request.getName()) && productRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Tên sản phẩm '" + request.getName() + "' đã tồn tại");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setStockQuantity(request.getStockQuantity());
        product.setPriceImport(request.getPriceImport());
        product.setPriceSelling(request.getPriceSelling());
        product.setStrapType(request.getStrapType());
        product.setMovementType(request.getMovementType());
        product.setCaseSize(request.getCaseSize());
        product.setThickness(request.getThickness());
        product.setGlassMaterial(request.getGlassMaterial());
        product.setCaseMaterial(request.getCaseMaterial());
        product.setWaterResistance(request.getWaterResistance());
        product.setWarranty(request.getWarranty());

        Optional<Brand> brand = brandRepository.findById(request.getBrandId());
        if (brand.isEmpty()) {
            throw new RuntimeException("Thương hiệu không hợp lệ");
        }
        product.setBrand(brand.get());

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : request.getCategoryIds()) {
                Optional<Category> category = categoryRepository.findById(categoryId);
                if (category.isPresent()) {
                    categories.add(category.get());
                } else {
                    throw new RuntimeException("Danh mục với ID " + categoryId + " không tồn tại");
                }
            }
            product.setCategories(categories);
        }
        product.setUpdateAt(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);

        return convertToProductResponse(savedProduct);
    }

    public void deleteProduct(String token, Long id) {
        tokenService.validateRole(token, Role.ADMIN.name());
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id = " + id));

        for (ProductImages image : product.getImages()) {
            imageService.deleteImage(image.getImageUrl());
        }
        productRepository.deleteById(id);
    }

    private boolean containsIgnoreCase(String field, String keyword) {
        return field != null && field.toLowerCase().contains(keyword);
    }

    private Comparator<Product> getProductComparator(String sortBy) {
        Comparator<Product> comparator = null;

        if ("price_asc".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Product::getPriceSelling);
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Product::getPriceSelling).reversed();
        } else if ("name_asc".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
        } else if ("name_desc".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER).reversed();
        }
        return comparator;
    }

    private String generateProductCode() {
        String prefix = "SP_";
        Optional<Product> lastProduct = productRepository.findTopByOrderByIdDesc();

        long nextId = lastProduct.map(product -> product.getId() + 1).orElse(1L);

        return prefix + String.format("%03d", nextId);
    }

    private ProductResponse convertToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setProductCode(product.getProductCode());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setStockQuantity(product.getStockQuantity());
        response.setPriceImport(product.getPriceImport());
        response.setPriceSelling(product.getPriceSelling());
        response.setStrapType(product.getStrapType());
        response.setMovementType(product.getMovementType());
        response.setCaseSize(product.getCaseSize());
        response.setThickness(product.getThickness());
        response.setGlassMaterial(product.getGlassMaterial());
        response.setCaseMaterial(product.getCaseMaterial());
        response.setWaterResistance(product.getWaterResistance());
        response.setWarranty(product.getWarranty());
        response.setCreatedAt(product.getCreateAt());
        response.setUpdatedAt(product.getUpdateAt());

        Brand brand = product.getBrand();
        BrandResponse brandDTO = new BrandResponse();
        brandDTO.setId(brand.getId());
        brandDTO.setName(brand.getName());
        response.setBrand(brandDTO);

        List<CategoryResponse> categoryResponses = new ArrayList<>();
        for (Category category : product.getCategories()) {
            CategoryResponse categoryDTO = new CategoryResponse();
            categoryDTO.setId(category.getId());
            categoryDTO.setName(category.getName());
            categoryResponses.add(categoryDTO);
        }
        response.setCategories(categoryResponses);

        List<String> imageUrls = new ArrayList<>();
        for (ProductImages image : product.getImages()) {
            imageUrls.add(image.getImageUrl());
        }
        response.setImageUrls(imageUrls);

        return response;
    }
}
