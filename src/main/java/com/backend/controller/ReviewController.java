package com.backend.controller;

import com.backend.dto.request.ReviewRequest;
import com.backend.dto.response.ReviewResponse;
import com.backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public List<ReviewResponse> getAllReviews(@RequestHeader("Authorization") String token) {
        return reviewService.getAllReviews(token);
    }

    @GetMapping("/{id}")
    public ReviewResponse getReviewById(@RequestHeader("Authorization") String token,
                                        @PathVariable Long id) {
        return reviewService.getReviewById(token, id);
    }

    @GetMapping("/user/{userId}")
    public List<ReviewResponse> getReviewsByUserId(@PathVariable Long userId) {
        return reviewService.getReviewsByUserId(userId);
    }

    @GetMapping("/product/{productId}")
    public List<ReviewResponse> getReviewsByProductId(@PathVariable Long productId) {
        return reviewService.getReviewsByProductId(productId);
    }

    @PostMapping
    public ReviewResponse createReview(@RequestHeader("Authorization") String token,
                                       @RequestBody ReviewRequest request) {
        return reviewService.createReview(token, request);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@RequestHeader("Authorization") String token,
                             @PathVariable Long id) {
        reviewService.deleteReview(token, id);
    }
}
