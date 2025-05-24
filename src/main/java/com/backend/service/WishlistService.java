package com.backend.service;

import com.backend.dto.request.WishlistRequest;
import com.backend.dto.response.ProductResponse;
import com.backend.dto.response.UserResponse;
import com.backend.dto.response.WishlistResponse;
import com.backend.model.*;
import com.backend.repository.ProductRepository;
import com.backend.repository.UserRepository;
import com.backend.repository.WishlistRepository;
import com.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenService tokenService;

    public List<WishlistResponse> getWishlistByUser(String token) {
        tokenService.validateRole(token, Role.USER.name());

        String subject = jwtUtil.extractSubject(token);
        User user = userRepository.findByEmail(subject)
                .orElseGet(() -> userRepository.findByPhone(subject)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng")));

        List<Wishlist> wishlists = wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return wishlists.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public WishlistResponse addToWishlist(String token, WishlistRequest request) {
        tokenService.validateRole(token, Role.USER.name());

        String subject = jwtUtil.extractSubject(token);
        User user = userRepository.findByEmail(subject)
                .orElseGet(() -> userRepository.findByPhone(subject)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng")));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Sản phẩm đã có trong wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);

        return convertToResponse(wishlistRepository.save(wishlist));
    }

    public void removeFromWishlist(String token, Long productId) {
        tokenService.validateRole(token, Role.USER.name());

        String subject = jwtUtil.extractSubject(token);
        User user = userRepository.findByEmail(subject)
                .orElseGet(() -> userRepository.findByPhone(subject)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng")));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        wishlistRepository.deleteByUserAndProduct(user, product);
    }

    private WishlistResponse convertToResponse(Wishlist wishlist) {
        WishlistResponse response = new WishlistResponse();
        response.setId(wishlist.getId());
        response.setCreatedAt(wishlist.getCreatedAt());

        User user = wishlist.getUser();
        UserResponse userDto = new UserResponse();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setPhone(user.getPhone());
        userDto.setGender(user.getGender());
        userDto.setImgUrl(user.getImgUrl());
        userDto.setAddress(user.getAddress());
        userDto.setRole(user.getRole());
        userDto.setIsActive(user.getIsActive());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());
        response.setUser(userDto);

        Product product = wishlist.getProduct();
        ProductResponse productDto = new ProductResponse();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setPriceSelling(product.getPriceSelling());
        productDto.setImageUrls(
                product.getImages().stream()
                        .map(ProductImages::getImageUrl)
                        .collect(Collectors.toList())
        );
        response.setProduct(productDto);

        return response;
    }
}
