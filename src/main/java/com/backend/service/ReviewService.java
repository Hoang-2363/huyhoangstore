package com.backend.service;

import com.backend.dto.request.ReviewRequest;
import com.backend.dto.response.ProductResponse;
import com.backend.dto.response.ReviewResponse;
import com.backend.dto.response.UserResponse;
import com.backend.model.*;
import com.backend.repository.ProductRepository;
import com.backend.repository.ReviewRepository;
import com.backend.repository.UserRepository;
import com.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    public List<ReviewResponse> getAllReviews(String token) {
        tokenService.validateRole(token, Role.ADMIN.name());
        List<Review> reviews = reviewRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        return reviews.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public ReviewResponse getReviewById(String token, Long id) {
        tokenService.validateRole(token, Role.ADMIN.name());

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá với id = " + id));

        return convertToResponse(review);
    }

    public List<ReviewResponse> getReviewsByUserId(Long userId) {
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsByProductId(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        return reviews.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public ReviewResponse createReview(String token, ReviewRequest request) {
        tokenService.validateRole(token, Role.USER.name());
        String subject = jwtUtil.extractSubject(token);

        Optional<User> userByEmail = userRepository.findByEmail(subject);
        Optional<User> userByPhone = userRepository.findByPhone(subject);

        User user = userByEmail.orElseGet(() -> userByPhone
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!")));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id = " + request.getProductId()));

        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi.");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());

        return convertToResponse(reviewRepository.save(review));
    }

    public void deleteReview(String token, Long id) {
        tokenService.validateRole(token, Role.ADMIN.name());
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy đánh giá để xóa");
        }
        reviewRepository.deleteById(id);
    }

    private ReviewResponse convertToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());

        User user = review.getUser();
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

        Product product = review.getProduct();
        ProductResponse productDto = new ProductResponse();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setPriceSelling(product.getPriceSelling());
        productDto.setImageUrls(
                product.getImages()
                        .stream()
                        .map(ProductImages::getImageUrl)
                        .collect(Collectors.toList())
        );
        response.setProduct(productDto);

        return response;
    }
}