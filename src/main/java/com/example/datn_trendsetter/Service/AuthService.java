package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.AuthResponse;
import com.example.datn_trendsetter.DTO.LoginRequest;
import com.example.datn_trendsetter.DTO.RegisterRequest;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.security.JwtUtil;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final KhachHangRepository khachHangRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(KhachHangRepository khachHangRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.khachHangRepository = khachHangRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (khachHangRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword()); // Mã hóa mật khẩu

        KhachHang newUser = new KhachHang();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(encodedPassword); // Lưu mật khẩu mã hóa

        khachHangRepository.save(newUser);

        String token = jwtUtil.generateToken(newUser.getUsername());

        return new AuthResponse(token);
    }


    public AuthResponse login(LoginRequest request) {
        KhachHang user = khachHangRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token);
    }
}
