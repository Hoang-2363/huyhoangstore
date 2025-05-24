package com.backend.service;

import com.backend.dto.request.UserRequest;
import com.backend.dto.request.UserUpdateRequest;
import com.backend.model.Role;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import com.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ImageService imageService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenService tokenService;

    private Long getIdSuperAdmin(String token) {
        String subject = jwtUtil.extractSubject(token);

        Optional<User> userByEmail = userRepository.findByEmail(subject);
        Optional<User> userByPhone = userRepository.findByPhone(subject);

        User user = userByEmail.orElseGet(() -> userByPhone
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!")));
        return user.getId();
    }

    public List<User> getAllUsers(String token) {
        tokenService.validateRole(token, Role.ADMIN.name());
        if (getIdSuperAdmin(token) == 1) {
            return userRepository.findAllByIdNot(1L, Sort.by(Sort.Direction.ASC, "id"));
        }
        return userRepository.findAllByRole(Role.USER, Sort.by(Sort.Direction.ASC, "id"));
    }

    public User getUserById(String token, Long id) {
        tokenService.validateRole(token, Role.ADMIN.name());

        Long currentAdminId = getIdSuperAdmin(token);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với id = " + id));

        if (!currentAdminId.equals(1L) && user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không có quyền truy cập người dùng với id = " + id);
        }

        return user;
    }

    public List<User> searchUsers(String token, String keyword) {
        tokenService.validateRole(token, Role.ADMIN.name());
        Long currentAdminId = getIdSuperAdmin(token);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        List<User> users;
        if (currentAdminId == 1) {
            return userRepository.findAllByIdNot(1L, sort);
        } else {
            users = userRepository.findAllByRole(Role.USER, sort);
        }

        String lowerKeyword = keyword.toLowerCase();
        return users.stream()
                .filter(user -> user.getName().toLowerCase().contains(lowerKeyword)
                        || user.getEmail().toLowerCase().contains(lowerKeyword)
                        || user.getPhone().contains(keyword))
                .toList();
    }

    public User createUser(String token, UserRequest request, MultipartFile file) throws IOException {
        tokenService.validateRole(token, Role.ADMIN.name());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống!");
        }

        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại trong hệ thống!");
        }

        if (!tokenService.checkEmailExistsOnGoogle(request.getEmail())) {
            throw new RuntimeException("Email không tồn tại trên Google");
        }
        Long currentAdminId = getIdSuperAdmin(token);

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (currentAdminId == 1) {
            user.setRole(request.getRole());
        } else {
            user.setRole(Role.USER);
        }

        if (file != null && !file.isEmpty()) {
            String folder = (user.getRole() == Role.ADMIN) ? "admins" : "users";
            String imageUrl = imageService.uploadImage(file, folder);
            user.setImgUrl(imageUrl);
        } else {
            user.setImgUrl(request.getImgUrl());
        }

        return userRepository.save(user);
    }

    public User updateUser(String token, Long id, UserUpdateRequest request, MultipartFile file) throws IOException {
        tokenService.validateRole(token, Role.ADMIN.name());
        Long currentAdminId = getIdSuperAdmin(token);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với id = " + id));

        if (!currentAdminId.equals(1L) && user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không có quyền truy cập người dùng với id = " + id);
        }

        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại: " + request.getEmail());
        }

        if (!user.getPhone().equals(request.getPhone()) &&
                userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại: " + request.getPhone());
        }

        if (file != null && !file.isEmpty()) {
            imageService.deleteImage(user.getImgUrl());
            String folder = (user.getRole() == Role.ADMIN) ? "admins" : "users";
            String newImageUrl = imageService.uploadImage(file, folder);
            user.setImgUrl(newImageUrl);
        } else {
            user.setImgUrl(request.getImgUrl());
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (currentAdminId == 1) {
            user.setRole(request.getRole());
        }
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public void setUserIsActive(String token, Long id, boolean isActive) {
        tokenService.validateRole(token, Role.ADMIN.name());

        Long currentAdminId = getIdSuperAdmin(token);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với id = " + id));

        if (!currentAdminId.equals(1L) && user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không có quyền truy cập người dùng với id = " + id);
        }
        user.setIsActive(isActive);
        userRepository.save(user);
    }
}
