package com.backend.service;

import com.backend.dto.request.LoginRequest;
import com.backend.dto.request.ResetPasswordRequest;
import com.backend.dto.request.UserRequest;
import com.backend.dto.request.UserUpdateRequest;
import com.backend.model.Role;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import com.backend.security.JwtUtil;
import com.google.cloud.storage.Blob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.*;
import java.util.logging.Logger;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ImageService imageService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailService emailService;

    private final ConcurrentHashMap<String, String> otpStorage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<String, ScheduledFuture<?>> otpExpiryTasks = new ConcurrentHashMap<>();

    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    public User register(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống!");
        }

        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại trong hệ thống!");
        }

        if (!tokenService.checkEmailExistsOnGoogle(request.getEmail())) {
            throw new RuntimeException("Email không tồn tại trên Google");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        Optional<User> userByEmail = userRepository.findByEmail(request.getContact());
        Optional<User> userByPhone = userRepository.findByPhone(request.getContact());

        User existingUser = null;
        if (userByEmail.isPresent()) {
            existingUser = userByEmail.get();
        } else if (userByPhone.isPresent()) {
            existingUser = userByPhone.get();
        }

        if (existingUser == null || !passwordEncoder.matches(request.getPassword(), existingUser.getPassword())) {
            throw new RuntimeException("Thông tin đăng nhập không đúng, tài khoản hoặc mật khẩu bị sai!");
        }

        if (existingUser.getIsActive() != null && !existingUser.getIsActive()) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ tổng đài hệ thống!");
        }

        LocalDateTime tokenExpirationTime = LocalDateTime.now().plusHours(24);
//        LocalDateTime tokenExpirationTime = LocalDateTime.now().plusMinutes(1);
        existingUser.setTokenExpiryTime(tokenExpirationTime);

        String subject = existingUser.getEmail() != null ? existingUser.getEmail() : existingUser.getPhone();
        String token = jwtUtil.generateToken(subject, existingUser.getRole().name());
        existingUser.setToken(token);
        return userRepository.save(existingUser);
    }

    public void logout(String token) {
        String subject = jwtUtil.extractSubject(token);

        Optional<User> userByEmail = userRepository.findByEmail(subject);
        Optional<User> userByPhone = userRepository.findByPhone(subject);

        User user = userByEmail.orElseGet(() -> userByPhone
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!")));

        user.setToken(null);
        user.setTokenExpiryTime(null);
        userRepository.save(user);
    }

    public void generateOtp(ResetPasswordRequest request) {
        Random random = new Random();
        if (userRepository.findByEmail(request.getEmail()).isEmpty()) {
            throw new RuntimeException("Email không tồn tại trong hệ thống");
        }

        String otp = String.format("%06d", random.nextInt(999999));
        otpStorage.put(request.getEmail(), otp);

        ScheduledFuture<?> task = scheduler.schedule(() -> otpStorage.remove(request.getEmail()), 5, TimeUnit.MINUTES);
        otpExpiryTasks.put(request.getEmail(), task);

        emailService.sendEmail(request.getEmail(), "Mã OTP của bạn", "Mã OTP của bạn là: " + otp + "\nMã sẽ hết hạn sau 5 phút.");
    }

    public String resetPassword(ResetPasswordRequest request) {
        if (!verifyOtp(request.getEmail(), request.getOtp())) {
            throw new RuntimeException("Mã xác nhận không hợp lệ hoặc đã hết hạn.");
        }

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Email không tồn tại trong hệ thống");
        }

        User user = optionalUser.get();
        if (!request.getReNewPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("Nhập lại mật khẩu và mật khẩu mới không trùng nhau");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu đã được sử dụng");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        otpStorage.remove(request.getEmail());
        otpExpiryTasks.remove(request.getEmail());

        return "Mật khẩu đã được cập nhật thành công.";
    }

    public User update(String token, UserUpdateRequest request, MultipartFile file) throws IOException {
        String subject = jwtUtil.extractSubject(token);

        Optional<User> userByEmail = userRepository.findByEmail(subject);
        Optional<User> userByPhone = userRepository.findByPhone(subject);

        User user = userByEmail.orElseGet(() -> userByPhone
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!")));

        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!tokenService.checkEmailExistsOnGoogle(request.getEmail())) {
                throw new RuntimeException("Email không tồn tại trên Google");
            }
            if (userRepository.findByEmail(request.getEmail()).isPresent() && !user.getEmail().equals(request.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng!");
            }
            user.setEmail(request.getEmail());
        }

        String role = user.getRole().name().toLowerCase() + "s";
        if (file != null && !file.isEmpty()) {
            imageService.deleteImage(user.getImgUrl());
            String newImageUrl = imageService.uploadImage(file, role);
            user.setImgUrl(newImageUrl);
        } else {
            user.setImgUrl(user.getImgUrl());
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (userRepository.findByPhone(request.getPhone()).isPresent() && !user.getPhone().equals(request.getPhone())) {
                throw new RuntimeException("Số điện thoại đã được sử dụng!");
            }
            user.setPhone(request.getPhone());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setAddress(request.getAddress());
        user.setGender(request.getGender());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public boolean verifyOtp(String email, String otp) {
        return otpStorage.containsKey(email) && otpStorage.get(email).equals(otp);
    }
}
