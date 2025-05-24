package com.backend.controller;

import com.backend.dto.request.BillRequest;
import com.backend.service.EmailService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-bill")
    public ResponseEntity<?> sendBill(@RequestBody BillRequest billRequest) {
        try {
            emailService.sendBillEmail(billRequest);
            return ResponseEntity.ok().body(new ApiResponse("success", "Gửi hóa đơn thành công!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("error", "Lỗi gửi hóa đơn: " + e.getMessage()));
        }
    }

    @Getter
    private static class ApiResponse {
        private final String status;
        private final String message;

        public ApiResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

    }
}