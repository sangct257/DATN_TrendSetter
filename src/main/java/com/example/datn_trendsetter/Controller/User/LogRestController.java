package com.example.datn_trendsetter.Controller.User;

import com.example.datn_trendsetter.DTO.AuthResponse;
import com.example.datn_trendsetter.DTO.LoginRequest;
import com.example.datn_trendsetter.DTO.RegisterRequest;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class LogRestController {
    private final AuthService authService;

    public LogRestController(AuthService authService) {
        this.authService = authService;
    }

    // Đăng ký Nhân viên
    @PostMapping("/nhanvien/register")
    public ResponseEntity<?> registerNhanVien(@RequestBody RegisterRequest request, HttpSession session) {
        try {
            request.setLoaiTaiKhoan("NHANVIEN");
            AuthResponse response = authService.register(request, session);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "redirect", response.getRedirectUrl(),
                    "user", response.getUserDetails(),
                    "roles", response.getRoles()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Đăng ký Khách hàng
    @PostMapping("/khachhang/register")
    public ResponseEntity<?> registerKhachHang(@RequestBody RegisterRequest request, HttpSession session) {
        try {
            request.setLoaiTaiKhoan("KHACHHANG");
            AuthResponse response = authService.register(request, session);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "redirect", response.getRedirectUrl(),
                    "user", response.getUserDetails(),
                    "roles", response.getRoles()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Đăng nhập Nhân viên
    @PostMapping("/nhanvien/login")
    public ResponseEntity<?> loginNhanVien(@RequestBody LoginRequest request, HttpSession session) {
        try {
            request.setLoaiTaiKhoan("NHANVIEN");
            AuthResponse response = authService.login(request, session);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "redirect", response.getRedirectUrl(),
                    "user", response.getUserDetails(),
                    "roles", response.getRoles()

            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Đăng nhập Khách hàng
    @PostMapping("/khachhang/login")
    public ResponseEntity<?> loginKhachHang(@RequestBody LoginRequest request, HttpSession session) {
        try {
            request.setLoaiTaiKhoan("KHACHHANG");
            AuthResponse response = authService.login(request, session);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "redirect", response.getRedirectUrl(),
                    "user", response.getUserDetails(),
                    "roles", response.getRoles()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Đăng xuất thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi đăng xuất: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        if (isAuthenticated != null && isAuthenticated) {
            Object user = session.getAttribute("user");
            String userType = (user instanceof NhanVien) ? "NHANVIEN" :
                    (user instanceof KhachHang) ? "KHACHHANG" : "UNKNOWN";

            Map<String, Object> response = new HashMap<>();
            response.put("isAuthenticated", true);
            response.put("user", user);
            response.put("userType", userType);
            response.put("roles", session.getAttribute("roles"));
            response.put("accountType", session.getAttribute("accountType"));

            Long loginTime = (Long) session.getAttribute("loginTime");
            if (loginTime != null) {
                response.put("sessionTime", (System.currentTimeMillis() - loginTime) / 1000 + " giây");
            }

            return ResponseEntity.ok().body(response);
        }
        return ResponseEntity.ok().body(Collections.singletonMap("isAuthenticated", false));
    }
}