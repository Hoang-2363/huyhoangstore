package com.backend.service;

import com.backend.model.User;
import com.backend.repository.UserRepository;
import com.backend.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class TokenService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public void validateRole(String token, String role) {
        String userRole = jwtUtil.extractRole(token);
        String subject = jwtUtil.extractSubject(token);

        Optional<User> userByEmail = userRepository.findByEmail(subject);
        Optional<User> userByPhone = userRepository.findByPhone(subject);

        User user = userByEmail.orElseGet(() -> userByPhone
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!")));

        if (user.getTokenExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token của bạn đã hết hạn. Vui lòng đăng nhập lại!");
        }

        if (user.getToken() == null || !user.getToken().equals(token)) {
            throw new RuntimeException("Token không hợp lệ hoặc phiên đăng nhập đã hết!");
        }

        if (!role.equals(userRole)) {
            throw new RuntimeException("Bạn không có quyền thực hiện thao tác này!");
        }
    }

    public String cleanToken(String token) {
        return token.replace("Bearer ", "").trim();
    }

    public boolean checkEmailExistsOnGoogle(String email) {
        String apiKey = "5ea59dc36194c940feb74f121af29007972d4680";
        String url = "https://api.hunter.io/v2/email-verifier?email=" + email + "&api_key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String result = response.getBody();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(result);
                String status = jsonNode.get("data").get("status").asText();
                return status.equals("valid");
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
