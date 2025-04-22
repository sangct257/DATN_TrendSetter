package com.example.datn_trendsetter.Service;

public interface UserService {
    boolean existsByEmail(String email);
    String generateResetToken(String email);
    boolean resetPassword(String token, String newPassword);
}
