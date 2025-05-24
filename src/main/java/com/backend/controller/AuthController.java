package com.backend.controller;

import com.backend.dto.request.LoginRequest;
import com.backend.dto.request.ResetPasswordRequest;
import com.backend.dto.request.UserRequest;
import com.backend.dto.request.UserUpdateRequest;
import com.backend.model.User;
import com.backend.service.AuthService;
import com.backend.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @Autowired
    TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid UserRequest request) {
        User registeredUser = authService.register(request);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody @Valid LoginRequest request) {
        User loggedInUser = authService.login(request);
        return ResponseEntity.ok(loggedInUser);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        authService.logout(tokenService.cleanToken(token));
        return ResponseEntity.ok("Đăng xuất thành công!");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.generateOtp(request);
        return ResponseEntity.ok("Mã OTP đã được gửi đến email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        String result = authService.resetPassword(request);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update-account")
    public ResponseEntity<User> updateUser(
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute UserUpdateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        User updatedUser = authService.update(tokenService.cleanToken(token), request, file);
        return ResponseEntity.ok(updatedUser);
    }

}