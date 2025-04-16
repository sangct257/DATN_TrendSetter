package com.example.datn_trendsetter.Service;

public interface EmailService {
    void sendResetPasswordEmail(String to, String token);
}
