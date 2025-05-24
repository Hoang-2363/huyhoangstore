package com.backend.service;

import com.backend.dto.request.DiscountRequest;
import com.backend.model.Discount;
import com.backend.model.Role;
import com.backend.model.User;
import com.backend.repository.DiscountRepository;
import com.backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    public List<Discount> getAllDiscounts(String token) {
        tokenService.validateRole(token, Role.ADMIN.name());
        return discountRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Discount getDiscountById(String token, Long id) {
        tokenService.validateRole(token, Role.ADMIN.name());
        return discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá với ID = " + id));
    }

    public Discount getDiscountByCode(String code) {
        return discountRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá với mã là " + code));
    }

    public List<Discount> searchDiscounts(String token, String searchContent) {
        tokenService.validateRole(token, Role.ADMIN.name());
        return discountRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(discount ->
                        (discount.getCode() != null && discount.getCode().toLowerCase().contains(searchContent.toLowerCase())) ||
                                (discount.getDescription() != null && discount.getDescription().toLowerCase().contains(searchContent.toLowerCase()))
                )
                .collect(Collectors.toList());
    }

    public Discount createDiscount(String token, DiscountRequest request) {
        tokenService.validateRole(token, Role.ADMIN.name());

        Discount discount = new Discount();
        String code;
        if (request.getCode() == null || request.getCode().isBlank()) {
            code = generateUniqueDiscountCode();
        } else {
            code = request.getCode();
            while (discountRepository.findByCode(code).isPresent()) {
                code = generateUniqueDiscountCode();
            }
        }

        discount.setCode(code);
        discount.setDescription(request.getDescription());
        discount.setPercentAmount(request.getPercentAmount());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        discount.setIsActive(request.getIsActive());

        return discountRepository.save(discount);
    }

    public Discount updateDiscount(String token, Long id, DiscountRequest request) {
        tokenService.validateRole(token, Role.ADMIN.name());

        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá với ID = " + id));

        discount.setDescription(request.getDescription());
        discount.setPercentAmount(request.getPercentAmount());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        discount.setIsActive(request.getIsActive());
        discount.setUpdatedAt(LocalDateTime.now());

        return discountRepository.save(discount);
    }

    public void deleteDiscount(String token, Long id) {
        tokenService.validateRole(token, Role.ADMIN.name());

        if (!discountRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy mã giảm giá với ID = " + id);
        }

        discountRepository.deleteById(id);
    }

    public void notifyUsersAboutDiscount(String token, Discount discount) {
        tokenService.validateRole(token, Role.ADMIN.name());
        List<User> users = userRepository.findAllByRole(Role.USER, Sort.by(Sort.Direction.ASC, "id"));

        String subject = "Ưu đãi mới: " + discount.getCode();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedStartDate = discount.getStartDate().format(formatter);
        String formattedEndDate = discount.getEndDate().format(formatter);

        String body = String.format("""
                            <html>
                                <head>
                                    <style>
                                        .container {
                                            font-family: Arial, sans-serif;
                                            padding: 20px;
                                            background-color: #f4f4f4;
                                        }
                                        .email-box {
                                            max-width: 600px;
                                            margin: auto;
                                            background-color: #ffffff;
                                            padding: 20px;
                                            border-radius: 8px;
                                            box-shadow: 0 0 10px rgba(0,0,0,0.1);
                                        }
                                        h2 {
                                            color: #2c3e50;
                                        }
                                        .discount-code {
                                            font-size: 24px;
                                            font-weight: bold;
                                            color: #e74c3c;
                                            margin: 10px 0;
                                        }
                                        .info {
                                            font-size: 16px;
                                            margin: 5px 0;
                                        }
                                        .footer {
                                            margin-top: 20px;
                                            font-size: 14px;
                                            color: #777;
                                        }
                                    </style>
                                </head>
                                <body>
                                    <div class="container">
                                        <div class="email-box">
                                            <h2>🎉 Ưu đãi mới từ Đồng Hồ Store!</h2>
                                            <p>Xin chào,</p>
                                            <p>Bạn vừa nhận được một mã giảm giá mới từ cửa hàng đồng hồ của chúng tôi!</p>
                        
                                            <div class="discount-code">👉 Mã: %s</div>
                                            <div class="info">📉 Giảm: %.2f%%</div>
                                            <div class="info">⏳ Hiệu lực: từ %s đến %s</div>
                                            <div class="info"><strong>Mô tả:</strong> %s</div>
                        
                                            <p>Hãy nhanh tay sử dụng tại trang thanh toán nhé!</p>
                        
                                            <div class="footer">
                                                Trân trọng,<br/>
                                                <strong>Đồng Hồ Store</strong>
                                            </div>
                                        </div>
                                    </div>
                                </body>
                            </html>
                        """,
                discount.getCode(),
                discount.getPercentAmount().doubleValue(),
                formattedStartDate,
                formattedEndDate,
                discount.getDescription()
        );

        users.forEach(user -> {
            try {
                emailService.sendHtmlEmail(user.getEmail(), subject, body);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
    }

    private String generateUniqueDiscountCode() {
        String code;
        do {
            code = generateDiscountCode();
        } while (discountRepository.findByCode(code).isPresent());
        return code;
    }

    private String generateDiscountCode() {
        int length = 9;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }
}
