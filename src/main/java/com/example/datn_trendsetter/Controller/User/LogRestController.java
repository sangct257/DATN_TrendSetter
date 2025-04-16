package com.example.datn_trendsetter.Controller.User;

import com.example.datn_trendsetter.DTO.AuthResponse;
import com.example.datn_trendsetter.DTO.LoginRequest;
import com.example.datn_trendsetter.DTO.RegisterRequest;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.Service.AuthService;
import com.example.datn_trendsetter.Service.EmailService;
import com.example.datn_trendsetter.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class LogRestController {

    private final UserService userService;
    private final EmailService emailService;


    private final AuthService authService;
    private final KhachHangRepository khachHangRepository;

    @Autowired
    public LogRestController(UserService userService, EmailService emailService, AuthService authService, KhachHangRepository khachHangRepository) {
        this.userService = userService;
        this.emailService = emailService;
        this.authService = authService;
        this.khachHangRepository = khachHangRepository;
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
                String accountType = (String) session.getAttribute("accountType");
                String redirectUrl = "/";

                if ("NHANVIEN".equals(accountType)) {
                    redirectUrl = "/auth/home";
                } else if ("KHACHHANG".equals(accountType)) {
                    redirectUrl = "/trang-chu";
                }

                authService.logout(session, response, accountType);

                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "Đăng xuất thành công",
                        "redirect", redirectUrl
                ));
            }

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

    @GetMapping("/khachhang/check-email")
    public ResponseEntity<?> checkEmailExists(@RequestParam("email") String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        if (!userService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Email không tồn tại"));
        }

        String token = userService.generateResetToken(email);
        emailService.sendResetPasswordEmail(email, token);

        return ResponseEntity.ok(Collections.singletonMap("message", "Email reset đã được gửi"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        boolean success = userService.resetPassword(token, newPassword);
        if (success) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Đặt lại mật khẩu thành công"));
        } else {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Token không hợp lệ hoặc đã hết hạn"));
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