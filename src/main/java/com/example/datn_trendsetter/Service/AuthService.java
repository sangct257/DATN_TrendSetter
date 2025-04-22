package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.AuthResponse;
import com.example.datn_trendsetter.DTO.LoginRequest;
import com.example.datn_trendsetter.DTO.RegisterRequest;
import com.example.datn_trendsetter.DTO.UserDetails;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.Repository.NhanVienRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
                    roles,
                    null,
                    null
            );
        } else {
            if (khachHangRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã tồn tại trong hệ thống khách hàng");
            }

            KhachHang khachHang = new KhachHang();
            khachHang.setEmail(request.getEmail());
            khachHang.setPassword(encodedPassword);
            khachHang.setHoTen(request.getHoTen());
            khachHang.setTrangThai("Đang Hoạt Động");
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
                    roles,
                    null,
                    null
            );
        }
    }
    public AuthResponse login(LoginRequest request, HttpSession session, HttpServletResponse response) {
        Optional<NhanVien> optionalNhanVien = nhanVienRepository.findByEmail(request.getEmail());
        if (optionalNhanVien.isPresent()) {
            NhanVien nhanVien = optionalNhanVien.get();

            if (!passwordEncoder.matches(request.getPassword(), nhanVien.getPassword())) {
                throw new RuntimeException("Email hoặc mật khẩu không đúng");
            }

            if (!"Đang Hoạt Động".equals(nhanVien.getTrangThai())) {
                throw new RuntimeException("Tài khoản của bạn không còn hoạt động");
            }

            String sessionId = UUID.randomUUID().toString();

            Cookie nhanVienCookie = new Cookie("SESSION_NHANVIEN", sessionId);
            nhanVienCookie.setMaxAge(60 * 60);
            nhanVienCookie.setPath("/");
            response.addCookie(nhanVienCookie);

            session.setAttribute("isAuthenticated", true);
            session.setAttribute("SESSION_NHANVIEN", sessionId);
            session.setAttribute("userNhanVien", nhanVien);
            session.setAttribute("rolesNhanVien", Collections.singletonList("ROLE_" + nhanVien.getVaiTro().name()));
            session.setAttribute("accountType", "NHANVIEN");

            return new AuthResponse(nhanVien, UserDetails.fromNhanVien(nhanVien), "/admin/",
                    Collections.singletonList("ROLE_" + nhanVien.getVaiTro().name()), sessionId, "NHANVIEN");
        }

        Optional<KhachHang> optionalKhachHang = khachHangRepository.findByEmail(request.getEmail());
        if (optionalKhachHang.isPresent()) {
            KhachHang khachHang = optionalKhachHang.get();

            if (!passwordEncoder.matches(request.getPassword(), khachHang.getPassword())) {
                throw new RuntimeException("Email hoặc mật khẩu không đúng");
            }

            if (!"Đang Hoạt Động".equals(khachHang.getTrangThai())) {
                throw new RuntimeException("Tài khoản của bạn không còn hoạt động");
            }

            String sessionId = UUID.randomUUID().toString();

            Cookie khachHangCookie = new Cookie("SESSION_KHACHHANG", sessionId);
            khachHangCookie.setMaxAge(60 * 60);
            khachHangCookie.setPath("/");
            response.addCookie(khachHangCookie);

            session.setAttribute("isAuthenticated", true);
            session.setAttribute("SESSION_KHACHHANG", sessionId);
            session.setAttribute("userKhachHang", khachHang);
            session.setAttribute("rolesKhachHang", Collections.singletonList("ROLE_KHACHHANG"));
            session.setAttribute("accountType", "KHACHHANG");

            return new AuthResponse(khachHang, UserDetails.fromKhachHang(khachHang), "/trang-chu",
                    Collections.singletonList("ROLE_KHACHHANG"), sessionId, "KHACHHANG");
        }

        throw new RuntimeException("Tài khoản không tồn tại trong hệ thống");
    }

    public void logout(HttpSession session, HttpServletResponse response, String accountType) {
        if ("NHANVIEN".equals(accountType)) {
            session.removeAttribute("isAuthenticated");
            session.removeAttribute("SESSION_NHANVIEN");
            session.removeAttribute("userNhanVien");
            session.removeAttribute("rolesNhanVien");

            Cookie cookie = new Cookie("SESSION_NHANVIEN", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);

        } else if ("KHACHHANG".equals(accountType)) {
            session.removeAttribute("isAuthenticated");
            session.removeAttribute("SESSION_KHACHHANG");
            session.removeAttribute("userKhachHang");
            session.removeAttribute("rolesKhachHang");

            Cookie cookie = new Cookie("SESSION_KHACHHANG", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        session.removeAttribute("accountType");
        session.invalidate();
    }

}