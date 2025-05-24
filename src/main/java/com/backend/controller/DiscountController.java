package com.backend.controller;

import com.backend.dto.request.DiscountRequest;
import com.backend.model.Discount;
import com.backend.service.DiscountService;
import com.backend.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private TokenService tokenService;

    @GetMapping
    public ResponseEntity<List<Discount>> getAllDiscounts(@RequestHeader("Authorization") String token) {
        List<Discount> discounts = discountService.getAllDiscounts(tokenService.cleanToken(token));
        return ResponseEntity.ok(discounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Discount> getDiscountById(@RequestHeader("Authorization") String token,
                                                    @PathVariable Long id) {
        Discount discount = discountService.getDiscountById(tokenService.cleanToken(token), id);
        return ResponseEntity.ok(discount);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Discount> getDiscountByCode(@PathVariable String code) {
        Discount discount = discountService.getDiscountByCode(code);
        return ResponseEntity.ok(discount);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Discount>> searchDiscounts(
            @RequestHeader("Authorization") String token,
            @RequestParam("q") String searchContent
    ) {
        List<Discount> discounts = discountService.searchDiscounts(tokenService.cleanToken(token), searchContent);
        return ResponseEntity.ok(discounts);
    }

    @PostMapping
    public ResponseEntity<Discount> createDiscount(@RequestHeader("Authorization") String token,
                                                   @Valid @RequestBody DiscountRequest request) {
        Discount created = discountService.createDiscount(tokenService.cleanToken(token), request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Discount> updateDiscount(@RequestHeader("Authorization") String token,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody DiscountRequest request) {
        Discount updated = discountService.updateDiscount(tokenService.cleanToken(token), id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscount(@RequestHeader("Authorization") String token,
                                               @PathVariable Long id) {
        discountService.deleteDiscount(tokenService.cleanToken(token), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/notify/{id}")
    public ResponseEntity<String> notifyUsersAboutDiscount(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {

        Discount discount = discountService.getDiscountById(tokenService.cleanToken(token), id);
        discountService.notifyUsersAboutDiscount(tokenService.cleanToken(token), discount);

        return ResponseEntity.ok("Đã gửi thông báo mã giảm giá tới tất cả người dùng.");
    }

}
