package com.backend.service;

import com.backend.dto.request.BillRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    public void sendBillEmail(BillRequest billRequest) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(billRequest.getEmailUser());
        helper.setSubject("Xác nhận đơn hàng của bạn - Đồng hồ Phương Nam");
        helper.setFrom("20213168@eaut.edu.vn");

        StringBuilder itemsHtml = new StringBuilder();
        for (var item : billRequest.getCartItems()) {
            String imageUrl = "https://img.pikbest.com/element_our/20230502/bg/5623fefc32183.png!sw800";
            if (item.getProduct().getImageUrls() != null && !item.getProduct().getImageUrls().isEmpty()) {
                imageUrl = item.getProduct().getImageUrls().getFirst();
            }
            itemsHtml.append(String.format(
                    "<tr>" +
                            "<td><img src='%s' alt='%s' style='width:50px; height:50px; object-fit:cover;'/></td>" +
                            "<td style='text-align: center;'>%s</td>" +
                            "<td style='text-align: center;'>%d</td>" +
                            "<td style='text-align: center;'>%,.0f₫</td>" +
                            "<td style='text-align: center;'>%,.0f₫</td>" +
                            "</tr>",
                    imageUrl,
                    item.getProduct().getName(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getProduct().getPriceSelling(),
                    item.getQuantity() * item.getProduct().getPriceSelling().doubleValue()
            ));
        }

        String mailContent = String.format(
                "<body style='margin: 0; padding: 20px 0; font-family: Arial, sans-serif; background-color: #f5f5f5;'>" +
                        "  <div class='container' style='max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px;'>" +

                        "    <!-- Header -->" +
                        "    <div style='text-align: center; margin-bottom: 30px;'>" +
                        "      <img src='https://firebasestorage.googleapis.com/v0/b/lab02-7fb8f.appspot.com/o/logo_watch.png?alt=media&token=cf5caed1-cc4d-4ca8-8c97-0b3f4e559b34' alt='Đồng hồ Phương Nam' style='height: 100px; width: 110px'>" +
                        "      <h1 style='color: #2a2a2a; margin: 15px 0;'>Đồng hồ Phương Nam</h1>" +
                        "    </div>" +

                        "    <!-- Order Confirmation -->" +
                        "    <h2 style='color: #2a2a2a; border-bottom: 2px solid #eee; padding-bottom: 10px;'>Xác nhận đơn hàng</h2>" +
                        "    <p style='color: #666; line-height: 1.6;'>Xin chào <strong>%s</strong>,</p>" +
                        "    <p style='color: #666; line-height: 1.6;'>Cảm ơn bạn đã đặt hàng! Dưới đây là chi tiết đơn hàng của bạn:</p>" +

                        "    <!-- Order Items -->" +
                        "    <h3 style='color: #2a2a2a; margin-top: 25px;'>Chi tiết đơn hàng</h3>" +
                        "    <table style='width: 100%%; border-collapse: collapse; margin: 20px 0;'>" +
                        "      <thead>" +
                        "        <tr style='background-color: #f8f9fa;'>" +
                        "          <th style='padding: 12px; text-align: left; width: 15%%;'>Hình ảnh</th>" +
                        "          <th style='padding: 12px; text-align: center;'>Sản phẩm</th>" +
                        "          <th style='padding: 12px; text-align: center; width: 15%%;'>Số lượng</th>" +
                        "          <th style='padding: 12px; text-align: center; width: 15%%;'>Đơn giá</th>" +
                        "          <th style='padding: 12px; text-align: center; width: 15%%;'>Thành tiền</th>" +
                        "        </tr>" +
                        "      </thead>" +
                        "      <tbody>%s</tbody>" +
                        "    </table>" +

                        "    <!-- Totals -->" +
                        "    <div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px;'>" +
                        "      <div style='display: flex; justify-content: space-between; margin: 10px 0;'>" +
                        "        <span>Tổng sản phẩm:&nbsp;&nbsp;&nbsp;</span>" +
                        "        <strong>%d</strong>" +
                        "      </div>" +
                        "      <div style='display: flex; justify-content: space-between; margin: 10px 0;'>" +
                        "        <span>Tổng tiền:&nbsp;&nbsp;&nbsp;</span>" +
                        "        <strong style='color: #e74c3c; font-size: 1.2em;'>%,.0f₫</strong>" +
                        "      </div>" +
                        "    </div>" +

                        "    <!-- Customer Info -->" +
                        "    <h3 style='color: #2a2a2a; margin-top: 30px;'>Thông tin khách hàng</h3>" +
                        "    <div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin-top: 15px;'>" +
                        "      <div style='display: flex; margin-bottom: 10px;'>" +
                        "        <div style='width: 120px; color: #666;'>Họ và tên:</div>" +
                        "        <div>%s</div>" +
                        "      </div>" +
                        "      <div style='display: flex; margin-bottom: 10px;'>" +
                        "        <div style='width: 120px; color: #666;'>Email:</div>" +
                        "        <div>%s</div>" +
                        "      </div>" +
                        "      <div style='display: flex; margin-bottom: 10px;'>" +
                        "        <div style='width: 120px; color: #666;'>Điện thoại:</div>" +
                        "        <div>%s</div>" +
                        "      </div>" +
                        "      <div style='display: flex;'>" +
                        "        <div style='width: 120px; color: #666;'>Địa chỉ:</div>" +
                        "        <div>%s</div>" +
                        "      </div>" +
                        "    </div>" +

                        "    <!-- Footer -->" +
                        "    <div style='margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #666;'>" +
                        "      <p>Liên hệ với chúng tôi</p>" +
                        "      <p>📞 0349135499 - 0359782629 | 📍 31 P. Đinh Tiên Hoàng, Hàng Trống, Hoàn Kiếm, Hà Nội, Việt Nam</p>" +
                        "      <div style='margin-top: 15px;'>" +
                        "        <a href='#' style='margin: 0 10px; text-decoration: none;'>Facebook</a>" +
                        "        <a href='#' style='margin: 0 10px; text-decoration: none;'>Instagram</a>" +
                        "        <a href='#' style='margin: 0 10px; text-decoration: none;'>Website</a>" +
                        "      </div>" +
                        "    </div>" +

                        "  </div>" +
                        "</body>",
                billRequest.getNameUser(),
                itemsHtml,
                billRequest.getTotalItems(),
                billRequest.getTotalAmount(),
                billRequest.getNameUser(),
                billRequest.getEmailUser(),
                billRequest.getPhoneUser(),
                billRequest.getAddressUser()
        );

        helper.setText(mailContent, true);
        mailSender.send(mimeMessage);
    }
}
