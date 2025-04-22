package com.example.datn_trendsetter.Controller.User;

import com.example.datn_trendsetter.DTO.AuthResponse;
import com.example.datn_trendsetter.DTO.LoginRequest;
import com.example.datn_trendsetter.DTO.RegisterRequest;
import com.example.datn_trendsetter.DTO.ResetPasswordRequest;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.Service.AuthService;
import com.example.datn_trendsetter.Service.EmailService;
import com.example.datn_trendsetter.Service.KhachHangService;
import com.example.datn_trendsetter.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private KhachHangService khachHangService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public LogRestController(UserService userService, EmailService emailService,
                             AuthService authService, KhachHangRepository khachHangRepository) {
        this.userService = userService;
        this.emailService = emailService;
        this.authService = authService;
        this.khachHangRepository = khachHangRepository;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        if (!userService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email không tồn tại")
            );
        }

        String token = userService.generateResetToken(email);
        // Sửa thành link trỏ đến trang reset password của frontend
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(email, resetLink);

        return ResponseEntity.ok().body(
                Map.of("message", "Email khôi phục đã được gửi")
        );
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        // 1. Kiểm tra token và mật khẩu mới
        if (request.getToken() == null || request.getNewPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token và mật khẩu mới là bắt buộc"));
        }

        // 2. Tìm khách hàng bằng token (chỉ so sánh token, không bao gồm URL)
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByResetToken(request.getToken());
        if (!khachHangOpt.isPresent()) {
            // Log để debug
            System.err.println("Token không tồn tại trong DB: " + request.getToken());
            return ResponseEntity.status(400).body(Map.of("error", "Token không hợp lệ hoặc đã hết hạn"));
        }

        // 3. Cập nhật mật khẩu và xóa token
        KhachHang khachHang = khachHangOpt.get();
        khachHang.setPassword(passwordEncoder.encode(request.getNewPassword()));
        khachHang.setResetToken(null); // Xóa token sau khi dùng
        khachHangRepository.save(khachHang);

        return ResponseEntity.ok().body(Map.of("message", "Đặt lại mật khẩu thành công"));
    }


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

    @PostMapping("/khachhang/login")
    public ResponseEntity<?> loginKhachHang(@RequestBody LoginRequest request,
                                            HttpSession session, HttpServletResponse response) {
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

    // ==================== OTHER ENDPOINTS ====================

    @GetMapping("/khachhang/check-email")
    public ResponseEntity<?> checkEmailExists(@RequestParam("email") String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
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

    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");

        if (isAuthenticated != null && isAuthenticated) {
            Map<String, Object> response = new HashMap<>();

            // Get roles based on account type
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