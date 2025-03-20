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
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String loaiTaiKhoan = request.getLoaiTaiKhoan();

        if ("NHANVIEN".equalsIgnoreCase(loaiTaiKhoan)) {
            if (nhanVienRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã tồn tại trong hệ thống nhân viên");
            }
            NhanVien nhanVien = new NhanVien();
            nhanVien.setEmail(request.getEmail());
            nhanVien.setPassword(encodedPassword);
            nhanVien.setVaiTro(NhanVien.Role.NHANVIEN);
            nhanVienRepository.save(nhanVien);

            String token = jwtUtil.generateToken(nhanVien.getUsername(), nhanVien.getEmail(), "Nhân viên", "", "NHANVIEN");
            return new AuthResponse(token, "ROLE_NHANVIEN", "/admin/sell-counter");
        } else {
            if (khachHangRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã tồn tại trong hệ thống khách hàng");
            }
            KhachHang khachHang = new KhachHang();
            khachHang.setEmail(request.getEmail());
            khachHang.setPassword(encodedPassword);
            khachHangRepository.save(khachHang);

            String token = jwtUtil.generateToken(khachHang.getUsername(), khachHang.getEmail(), "Khách hàng", "", "KHACHHANG");
            return new AuthResponse(token, "ROLE_KHACHHANG", "/trang-chu");
        }
    }

    public AuthResponse login(LoginRequest request) {
        Optional<NhanVien> optionalNhanVien = nhanVienRepository.findByEmail(request.getEmail());
        if (optionalNhanVien.isPresent()) {
            NhanVien nhanVien = optionalNhanVien.get();
            if (!passwordEncoder.matches(request.getPassword(), nhanVien.getPassword())) {
                throw new RuntimeException("Thông tin không hợp lệ");
            }
            String token = jwtUtil.generateToken(nhanVien.getUsername(), nhanVien.getEmail(), "Nhân viên", "", "NHANVIEN");
            return new AuthResponse(token, "ROLE_NHANVIEN", "/admin/sell-counter");
        }

        Optional<KhachHang> optionalKhachHang = khachHangRepository.findByEmail(request.getEmail());
        if (optionalKhachHang.isPresent()) {
            KhachHang khachHang = optionalKhachHang.get();
            if (!passwordEncoder.matches(request.getPassword(), khachHang.getPassword())) {
                throw new RuntimeException("Thông tin không hợp lệ");
            }
            String token = jwtUtil.generateToken(khachHang.getUsername(), khachHang.getEmail(), "Khách hàng", "", "KHACHHANG");
            return new AuthResponse(token, "ROLE_KHACHHANG", "/trang-chu");
        }

        throw new RuntimeException("Tài khoản không tồn tại");
    }
}
