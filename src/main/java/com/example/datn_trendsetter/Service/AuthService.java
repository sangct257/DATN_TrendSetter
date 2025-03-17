package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.AuthResponse;
import com.example.datn_trendsetter.DTO.LoginRequest;
import com.example.datn_trendsetter.DTO.RegisterRequest;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.Repository.NhanVienRepository;
import com.example.datn_trendsetter.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(KhachHangRepository khachHangRepository, NhanVienRepository nhanVienRepository,
                       BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        String role = (request.getRole() != null && !request.getRole().isEmpty()) ? request.getRole().toUpperCase() : "KHACHHANG";
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        if (role.equals("ADMIN") || role.equals("NHANVIEN")) {
            if (nhanVienRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists in NhanVien");
            }

            NhanVien nhanVien = new NhanVien();
            nhanVien.setEmail(request.getEmail());
            nhanVien.setPassword(encodedPassword);
            nhanVien.setVaiTro(NhanVien.Role.valueOf(role));

            nhanVienRepository.save(nhanVien);
        } else {
            if (khachHangRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists in KhachHang");
            }

            KhachHang khachHang = new KhachHang();
            khachHang.setEmail(request.getEmail());
            khachHang.setPassword(encodedPassword);

            khachHangRepository.save(khachHang);
        }

        List<String> jwtRoles = (role.equals("KHACHHANG")) ? List.of("ROLE_KHACHHANG") : List.of("ROLE_" + role);
        String token = jwtUtil.generateToken(request.getEmail(), jwtRoles);

        return new AuthResponse(token, jwtRoles.get(0), getRedirectUrlByRole(jwtRoles.get(0)));
    }

    public AuthResponse login(LoginRequest request) {
        Optional<NhanVien> optionalNhanVien = nhanVienRepository.findByEmail(request.getEmail());
        if (optionalNhanVien.isPresent()) {
            NhanVien nhanVien = optionalNhanVien.get();
            if (!passwordEncoder.matches(request.getPassword(), nhanVien.getPassword())) {
                throw new RuntimeException("Thông tin không hợp lệ");
            }

            List<String> roles = List.of("ROLE_" + nhanVien.getVaiTro().name());
            String token = jwtUtil.generateToken(nhanVien.getEmail(), roles);

            return new AuthResponse(token, roles.get(0), getRedirectUrlByRole(roles.get(0)));
        }

        Optional<KhachHang> optionalKhachHang = khachHangRepository.findByEmail(request.getEmail());
        if (optionalKhachHang.isPresent()) {
            KhachHang khachHang = optionalKhachHang.get();
            if (!passwordEncoder.matches(request.getPassword(), khachHang.getPassword())) {
                throw new RuntimeException("Thông tin không hợp lệ");
            }

            List<String> roles = List.of("ROLE_KHACHHANG");
            String token = jwtUtil.generateToken(khachHang.getEmail(), roles);

            return new AuthResponse(token, "ROLE_KHACHHANG", getRedirectUrlByRole("ROLE_KHACHHANG"));
        }

        throw new RuntimeException("User not found");
    }

    private String getRedirectUrlByRole(String role) {
        return switch (role) {
            case "ROLE_ADMIN" -> "/admin/sell-counter";
            case "ROLE_NHANVIEN" -> "/admin/sell-counter";
            case "ROLE_KHACHHANG" -> "/trang-chu";
            default -> "/";
        };
    }
}
