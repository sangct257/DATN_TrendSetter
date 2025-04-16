package com.example.datn_trendsetter.Service.Impl;

import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.ResetToken;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.Repository.ResetTokenRepository;
import com.example.datn_trendsetter.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final KhachHangRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResetTokenRepository resetTokenRepository;

    @Autowired
    public UserServiceImpl(KhachHangRepository userRepository, PasswordEncoder passwordEncoder, ResetTokenRepository resetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.resetTokenRepository = resetTokenRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public String generateResetToken(String email) {
        String token = UUID.randomUUID().toString();
        KhachHang user = userRepository.findByEmail(email).orElseThrow();

        ResetToken resetToken = new ResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        resetTokenRepository.save(resetToken);

        return token;
    }

    @Override
    public boolean resetPassword(String token, String newPassword) {
        Optional<ResetToken> optionalToken = resetTokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) return false;

        ResetToken resetToken = optionalToken.get();
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) return false;

        KhachHang user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokenRepository.delete(resetToken); // Xóa token sau khi sử dụng
        return true;
    }
}
