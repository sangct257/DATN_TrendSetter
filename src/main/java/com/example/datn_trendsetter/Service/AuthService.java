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
            // Kiểm tra email đã tồn tại trong bảng nhân viên chưa
            if (nhanVienRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists in NhanVien");
            }

            // Thêm vào bảng Nhân viên
            NhanVien nhanVien = new NhanVien();
            nhanVien.setEmail(request.getEmail());
            nhanVien.setPassword(passwordEncoder.encode(request.getPassword()));

            if (role.equals("ADMIN") || role.equals("NHANVIEN")) {
                nhanVien.setVaiTro(NhanVien.Role.valueOf(role)); // Đổi thành NhanVien.Role
            } else {
                nhanVien.setVaiTro(NhanVien.Role.NHANVIEN); // Mặc định là NHANVIEN nếu không chọn
            }


            nhanVienRepository.save(nhanVien);
        } else {
            // Kiểm tra email đã tồn tại trong bảng khách hàng chưa
            if (khachHangRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists in KhachHang");
            }

            // Thêm vào bảng Khách hàng
            KhachHang khachHang = new KhachHang();
            khachHang.setEmail(request.getEmail());
            khachHang.setPassword(encodedPassword);

            khachHangRepository.save(khachHang);
        }

        // Tạo token với vai trò tương ứng
        String token = jwtUtil.generateToken(request.getEmail(), role);

        return new AuthResponse(token, role, "/trang-chu"); // Chuyển hướng sau khi đăng ký
    }



    public AuthResponse login(LoginRequest request) {
        // Kiểm tra xem user có trong bảng Nhân viên không
        Optional<NhanVien> optionalNhanVien = nhanVienRepository.findByEmail(request.getEmail());
        if (optionalNhanVien.isPresent()) {
            NhanVien nhanVien = optionalNhanVien.get();

            if (!passwordEncoder.matches(request.getPassword(), nhanVien.getPassword())) {
                throw new RuntimeException("Thông tin không hợp lệ");
            }

            // Lấy vai trò từ bảng Nhân viên
            String role = (nhanVien.getVaiTro() != null) ? nhanVien.getVaiTro().name() : "UNKNOWN";
            String token = jwtUtil.generateToken(nhanVien.getEmail(), role);
            System.out.println("Role khi tạo token: " + role);
            System.out.println("Token sinh ra: " + token);
            String redirectUrl = "/admin";
            return new AuthResponse(token, role, redirectUrl);

        }

        // Nếu không tìm thấy trong Nhân viên, kiểm tra trong Khách hàng
        Optional<KhachHang> optionalKhachHang = khachHangRepository.findByEmail(request.getEmail());
        if (optionalKhachHang.isPresent()) {
            KhachHang khachHang = optionalKhachHang.get();

            if (!passwordEncoder.matches(request.getPassword(), khachHang.getPassword())) {
                throw new RuntimeException("Thông tin không hợp lệ");
            }

            // Khách hàng không có vai trò, mặc định là "KHACHHANG"
            String token = jwtUtil.generateToken(khachHang.getEmail(), "KHACHHANG");

            return new AuthResponse(token, "KHACHHANG", "/trang-chu");
        }

        // Nếu không tìm thấy trong cả hai bảng
        throw new RuntimeException("User not found");
    }



}
