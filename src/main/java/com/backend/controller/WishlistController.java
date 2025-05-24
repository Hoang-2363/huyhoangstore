package com.backend.controller;

import com.backend.dto.request.WishlistRequest;
import com.backend.dto.response.WishlistResponse;
import com.backend.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping
    public List<WishlistResponse> getWishlist(@RequestHeader("Authorization") String token) {
        return wishlistService.getWishlistByUser(token);
    }

    @PostMapping
    public WishlistResponse addToWishlist(@RequestHeader("Authorization") String token,
                                          @RequestBody WishlistRequest request) {
        return wishlistService.addToWishlist(token, request);
    }

    @DeleteMapping("/{productId}")
    public void removeFromWishlist(@RequestHeader("Authorization") String token,
                                   @PathVariable Long productId) {
        wishlistService.removeFromWishlist(token, productId);
    }
}
