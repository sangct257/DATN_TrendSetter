package com.example.datn_trendsetter.Controller.User;

import com.example.datn_trendsetter.DTO.AuthResponse;
import com.example.datn_trendsetter.DTO.LoginRequest;
import com.example.datn_trendsetter.DTO.RegisterRequest;
import com.example.datn_trendsetter.Service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<?> registerNhanVien(@RequestBody RegisterRequest request) {
        try {
            request.setLoaiTaiKhoan("NHANVIEN"); // Xác định loại tài khoản là Nhân viên
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Đăng ký Khách hàng
    @PostMapping("/khachhang/register")
    public ResponseEntity<?> registerKhachHang(@RequestBody RegisterRequest request) {
        try {
            request.setLoaiTaiKhoan("KHACHHANG"); // Xác định loại tài khoản là Khách hàng
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @PostMapping("/nhanvien/login")
    public String loginNhanVien(@RequestBody LoginRequest request, HttpSession session) {
        try {
            request.setLoaiTaiKhoan("NHANVIEN");
            AuthResponse response = authService.login(request);

            // Lưu role vào session
            session.setAttribute("role", response.getRoles());

            return "redirect:" + response.getRedirectUrl();
        } catch (Exception e) {
            return  e.getMessage();
        }
    }
    // Đăng nhập Khách hàng
    @PostMapping("/khachhang/login")
    public ResponseEntity<?> loginKhachHang(@RequestBody LoginRequest request) {
        try {
            request.setLoaiTaiKhoan("KHACHHANG");
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + response.getToken())
                    .body(Map.of(
                            "redirect", response.getRedirectUrl(),
                            "token", response.getToken(),
                            "roles", response.getRoles()
                    ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Đăng xuất
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().body(Map.of("message", "Đăng xuất thành công"));
    }

    // Lấy thông tin người dùng từ token
    @GetMapping("/user-info")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_NHANVIEN', 'ROLE_KHACHHANG')")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "username", username,
                "roles", roles
        ));
    }
}
