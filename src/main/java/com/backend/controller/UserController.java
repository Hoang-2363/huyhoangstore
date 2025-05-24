package com.backend.controller;

import com.backend.dto.request.UserRequest;
import com.backend.dto.request.UserUpdateRequest;
import com.backend.model.User;
import com.backend.service.TokenService;
import com.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("Authorization") String token) {
        List<User> users = userService.getAllUsers(tokenService.cleanToken(token));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id
    ) {
        User user = userService.getUserById(tokenService.cleanToken(token), id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam("q") String keyword
    ) {
        List<User> users = userService.searchUsers(tokenService.cleanToken(token), keyword);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<User> createUser(
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute UserRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        User createdUser = userService.createUser(tokenService.cleanToken(token), request, file);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @ModelAttribute UserUpdateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        User updatedUser = userService.updateUser(tokenService.cleanToken(token), id, request, file);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/enable/{id}")
    public ResponseEntity<Void> setUserActiveStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestParam boolean isActive
    ) {
        userService.setUserIsActive(tokenService.cleanToken(token), id, isActive);
        return ResponseEntity.noContent().build();
    }
}
