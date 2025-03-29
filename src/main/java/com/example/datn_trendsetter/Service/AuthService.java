package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.AuthResponse;
import com.example.datn_trendsetter.DTO.LoginRequest;
import com.example.datn_trendsetter.DTO.RegisterRequest;
import com.example.datn_trendsetter.DTO.UserDetails;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.Repository.NhanVienRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(KhachHangRepository khachHangRepository,
                       NhanVienRepository nhanVienRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request, HttpSession session) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String loaiTaiKhoan = request.getLoaiTaiKhoan();

        if ("NHANVIEN".equalsIgnoreCase(loaiTaiKhoan)) {
            if (nhanVienRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã tồn tại trong hệ thống nhân viên");
            }

            NhanVien nhanVien = new NhanVien();
            nhanVien.setEmail(request.getEmail());
            nhanVien.setPassword(encodedPassword);
            nhanVien.setHoTen(request.getHoTen());

            if ("ADMIN".equalsIgnoreCase(request.getRole())) {
                nhanVien.setVaiTro(NhanVien.Role.ADMIN);
            } else {
                nhanVien.setVaiTro(NhanVien.Role.NHANVIEN);
            }

            NhanVien savedNhanVien = nhanVienRepository.save(nhanVien);
            UserDetails userDetails = UserDetails.fromNhanVien(savedNhanVien);
            List<String> roles = Collections.singletonList("ROLE_" + savedNhanVien.getVaiTro().name());

            session.setAttribute("user", savedNhanVien);
            session.setAttribute("userDetails", userDetails);
            session.setAttribute("roles", roles);

            String redirectUrl = savedNhanVien.getVaiTro() == NhanVien.Role.ADMIN
                    ? "/admin/"
                    : "/admin/sell-counter";

            return new AuthResponse(
                    savedNhanVien, // Truyền entity vào AuthResponse
                    userDetails,
                    redirectUrl,
                    roles
            );
        } else {
            if (khachHangRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã tồn tại trong hệ thống khách hàng");
            }

            KhachHang khachHang = new KhachHang();
            khachHang.setEmail(request.getEmail());
            khachHang.setPassword(encodedPassword);
            khachHang.setHoTen(request.getHoTen());
            KhachHang savedKhachHang = khachHangRepository.save(khachHang);

            UserDetails userDetails = UserDetails.fromKhachHang(savedKhachHang);
            List<String> roles = Collections.singletonList("ROLE_KHACHHANG");

            // Lưu cả entity và userDetails vào session
            session.setAttribute("user", savedKhachHang); // Lưu entity thay vì UserDetails
            session.setAttribute("userDetails", userDetails);
            session.setAttribute("roles", roles);

            return new AuthResponse(
                    savedKhachHang,
                    userDetails,
                    "/trang-chu",
                    roles
            );
        }
    }

    public AuthResponse login(LoginRequest request, HttpSession session) {
        Optional<NhanVien> optionalNhanVien = nhanVienRepository.findByEmail(request.getEmail());
        if (optionalNhanVien.isPresent()) {
            NhanVien nhanVien = optionalNhanVien.get();

            if (!passwordEncoder.matches(request.getPassword(), nhanVien.getPassword())) {
                throw new RuntimeException("Email hoặc mật khẩu không đúng");
            }

            System.out.println("Nhân viên đăng nhập thành công, ID: " + nhanVien.getId() + ", Vai trò: " + nhanVien.getVaiTro().name());

            UserDetails userDetails = UserDetails.fromNhanVien(nhanVien);
            List<String> roles = Collections.singletonList("ROLE_" + nhanVien.getVaiTro().name());

            session.setAttribute("isAuthenticated", true);
            session.setAttribute("user", nhanVien);
            session.setAttribute("userDetails", userDetails);
            session.setAttribute("roles", roles);

            return new AuthResponse(
                    nhanVien,
                    userDetails,
                    nhanVien.getVaiTro() == NhanVien.Role.ADMIN ? "/admin/sell-counter" : "/admin/",
                    roles
            );
        }

        Optional<KhachHang> optionalKhachHang = khachHangRepository.findByEmail(request.getEmail());
        if (optionalKhachHang.isPresent()) {
            KhachHang khachHang = optionalKhachHang.get();

            if (!passwordEncoder.matches(request.getPassword(), khachHang.getPassword())) {
                throw new RuntimeException("Email hoặc mật khẩu không đúng");
            }

            System.out.println("Khách hàng đăng nhập thành công, ID: " + khachHang.getId() + ", Vai trò: KHACHHANG");

            UserDetails userDetails = UserDetails.fromKhachHang(khachHang);
            List<String> roles = Collections.singletonList("ROLE_KHACHHANG");

            session.setAttribute("isAuthenticated", true);
            session.setAttribute("user", khachHang);
            session.setAttribute("userDetails", userDetails);
            session.setAttribute("roles", roles);

            return new AuthResponse(
                    khachHang,
                    userDetails,
                    "/trang-chu",
                    roles
            );
        }

        throw new RuntimeException("Tài khoản không tồn tại trong hệ thống");
    }
}