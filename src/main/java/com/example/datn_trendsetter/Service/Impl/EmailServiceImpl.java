package com.example.datn_trendsetter.Service.Impl;

import com.example.datn_trendsetter.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${frontend.reset-password-url}")
    private String resetPasswordUrl;

    @Override
    public void sendResetPasswordEmail(String to, String token) {
        String resetLink = resetPasswordUrl + "?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Đặt lại mật khẩu");
        message.setText("Nhấp vào liên kết sau để đặt lại mật khẩu: " + resetLink);

        mailSender.send(message);
    }
}
