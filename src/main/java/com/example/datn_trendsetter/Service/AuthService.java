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
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String loaiTaiKhoan = request.getLoaiTaiKhoan();

        if ("NHANVIEN".equalsIgnoreCase(loaiTaiKhoan)) {
            // Kiểm tra email đã tồn tại chưa
            if (nhanVienRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã tồn tại trong hệ thống nhân viên");
            }

            // Tạo mới nhân viên
            NhanVien nhanVien = new NhanVien();
            nhanVien.setEmail(request.getEmail());
            nhanVien.setPassword(encodedPassword);

            // Phân biệt vai trò Admin và Nhân viên
            if ("ADMIN".equalsIgnoreCase(request.getRole())) {
                nhanVien.setVaiTro(NhanVien.Role.ADMIN);
            } else {
                nhanVien.setVaiTro(NhanVien.Role.NHANVIEN);
            }

            // Lưu nhân viên vào database
            nhanVienRepository.save(nhanVien);

            // Tạo token và trả về AuthResponse
            String token = jwtUtil.generateToken(nhanVien.getEmail(), nhanVien.getEmail(), "Nhân viên", "", nhanVien.getVaiTro().name());
            String redirectUrl = nhanVien.getVaiTro() == NhanVien.Role.ADMIN ? "/admin/dashboard" : "/admin/sell-counter";
            return new AuthResponse(token, "ROLE_" + nhanVien.getVaiTro().name(), redirectUrl);
        } else {
            // Kiểm tra email đã tồn tại chưa
            if (khachHangRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã tồn tại trong hệ thống khách hàng");
            }

            // Tạo mới khách hàng
            KhachHang khachHang = new KhachHang();
            khachHang.setEmail(request.getEmail());
            khachHang.setPassword(encodedPassword);
            khachHangRepository.save(khachHang);

            // Tạo token và trả về AuthResponse
            String token = jwtUtil.generateToken(khachHang.getEmail(), khachHang.getEmail(), "Khách hàng", "", "KHACHHANG");
            return new AuthResponse(token, "ROLE_KHACHHANG", "/trang-chu");
        }
    }

    public AuthResponse login(LoginRequest request) {
        // Kiểm tra đăng nhập cho nhân viên
        Optional<NhanVien> optionalNhanVien = nhanVienRepository.findByEmail(request.getEmail());
        if (optionalNhanVien.isPresent()) {
            NhanVien nhanVien = optionalNhanVien.get();

            // Kiểm tra mật khẩu
            if (!passwordEncoder.matches(request.getPassword(), nhanVien.getPassword())) {
                throw new RuntimeException("Thông tin không hợp lệ");
            }

            // Tạo token và trả về AuthResponse
            String token = jwtUtil.generateToken(nhanVien.getEmail(), nhanVien.getEmail(), "Nhân viên", "", nhanVien.getVaiTro().name());
            String redirectUrl = nhanVien.getVaiTro() == NhanVien.Role.ADMIN ? "/admin/sell-counter" : "/admin/sell-counter";
            return new AuthResponse(token, "ROLE_" + nhanVien.getVaiTro().name(), redirectUrl);
        }

        // Kiểm tra đăng nhập cho khách hàng
        Optional<KhachHang> optionalKhachHang = khachHangRepository.findByEmail(request.getEmail());
        if (optionalKhachHang.isPresent()) {
            KhachHang khachHang = optionalKhachHang.get();

            // Kiểm tra mật khẩu
            if (!passwordEncoder.matches(request.getPassword(), khachHang.getPassword())) {
                throw new RuntimeException("Thông tin không hợp lệ");
            }

            // Tạo token và trả về AuthResponse
            String token = jwtUtil.generateToken(khachHang.getEmail(), khachHang.getEmail(), "Khách hàng", "", "KHACHHANG");
            return new AuthResponse(token, "ROLE_KHACHHANG", "/trang-chu");
        }

        // Nếu không tìm thấy tài khoản
        throw new RuntimeException("Tài khoản không tồn tại");
    }
}