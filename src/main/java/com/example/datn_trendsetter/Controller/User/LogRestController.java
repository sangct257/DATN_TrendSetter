package com.example.datn_trendsetter.Controller.User;

import com.example.datn_trendsetter.DTO.AuthResponse;
import com.example.datn_trendsetter.DTO.LoginRequest;
import com.example.datn_trendsetter.DTO.RegisterRequest;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    public ResponseEntity<?> loginNhanVien(@RequestBody LoginRequest request, HttpSession session, HttpServletResponse response) {
        try {
            request.setLoaiTaiKhoan("NHANVIEN");
            AuthResponse authResponse = authService.login(request, session, response);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "redirect", authResponse.getRedirectUrl(),
                    "user", authResponse.getUserDetails(),
                    "roles", authResponse.getRoles(),
                    "accountType", authResponse.getAccountType()
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
    public ResponseEntity<?> loginKhachHang(@RequestBody LoginRequest request, HttpSession session, HttpServletResponse response) {
        try {
            request.setLoaiTaiKhoan("KHACHHANG");
            AuthResponse authResponse = authService.login(request, session, response);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "redirect", authResponse.getRedirectUrl(),
                    "user", authResponse.getUserDetails(),
                    "roles", authResponse.getRoles(),
                    "accountType", authResponse.getAccountType()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                // Lấy loại tài khoản từ session
                String accountType = (String) session.getAttribute("accountType");

                // Kiểm tra tài khoản loại Nhân viên hoặc Khách hàng
                if (accountType != null) {
                    if (accountType.equals("NHANVIEN")) {
                        // Xử lý logout cho Nhân viên
                        System.out.println("Đang đăng xuất Nhân viên");
                        // Gọi phương thức logout cho Nhân viên
                        authService.logout(session, response, "SESSION_NHANVIEN");
                    } else if (accountType.equals("KHACHHANG")) {
                        // Xử lý logout cho Khách hàng
                        System.out.println("Đang đăng xuất Khách hàng");
                        // Gọi phương thức logout cho Khách hàng
                        authService.logout(session, response, "SESSION_KHACHHANG");
                    }
                }

                // Sau khi logout, trả về phản hồi thành công
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "Đăng xuất thành công"
                ));
            }

            // Nếu không có session, trả về lỗi
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy session"
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

        // Debugging thông tin session
        System.out.println("Session ID: " + session.getId());
        System.out.println("isAuthenticated: " + isAuthenticated);
        System.out.println("Session Role Nhan Vien: " + session.getAttribute("rolesNhanVien"));
        System.out.println("Session Role Khach Hang: " + session.getAttribute("rolesKhachHang"));

        if (isAuthenticated != null && isAuthenticated) {
            Map<String, Object> response = new HashMap<>();

            // Lấy roles tùy thuộc vào loại tài khoản
            List<String> roles = new ArrayList<>();
            if (session.getAttribute("rolesNhanVien") != null) {
                roles = (List<String>) session.getAttribute("rolesNhanVien");
            } else if (session.getAttribute("rolesKhachHang") != null) {
                roles = (List<String>) session.getAttribute("rolesKhachHang");
            }

            response.put("isAuthenticated", true);
            response.put("roles", roles);
            response.put("user", session.getAttribute("user"));
            response.put("accountType", session.getAttribute("accountType"));

            return ResponseEntity.ok().body(response);
        }

        return ResponseEntity.ok().body(Collections.singletonMap("isAuthenticated", false));
    }


}