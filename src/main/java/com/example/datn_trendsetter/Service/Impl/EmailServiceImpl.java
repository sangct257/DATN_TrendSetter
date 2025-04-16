package com.example.datn_trendsetter.Service.Impl;

import com.example.datn_trendsetter.Service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${frontend.reset-password-url}")
    private String resetPasswordUrl;

    @Override
    public void sendResetPasswordEmail(String to, String token) {
        try {
            String resetLink = resetPasswordUrl + "?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Đặt lại mật khẩu");
            helper.setText("<p>Nhấp vào liên kết sau để đặt lại mật khẩu:</p>"
                    + "<p><a href=\"" + resetLink + "\">Đặt lại mật khẩu</a></p>"
                    + "<p>Liên kết sẽ hết hạn sau 15 phút.</p>", true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
