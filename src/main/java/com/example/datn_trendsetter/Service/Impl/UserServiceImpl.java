package com.example.datn_trendsetter.Service.Impl;

import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.ResetToken;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.Repository.ResetTokenRepository;
import com.example.datn_trendsetter.Service.UserService;
import jakarta.transaction.Transactional;
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

    @Transactional
    public String generateResetToken(String email) {
        Optional<KhachHang> userOptional = userRepository.findByEmail(email);

        if (!userOptional.isPresent()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        KhachHang user = userOptional.get();

        resetTokenRepository.deleteByUserId(Long.valueOf(user.getId()));

        ResetToken resetToken = new ResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        resetTokenRepository.save(resetToken);
        return resetToken.getToken();
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

        resetTokenRepository.delete(resetToken);
        return true;
    }
}
