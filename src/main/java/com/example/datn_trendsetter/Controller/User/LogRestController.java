package com.example.datn_trendsetter.Controller.User;

import com.example.datn_trendsetter.DTO.AuthResponse;
import com.example.datn_trendsetter.DTO.LoginRequest;
import com.example.datn_trendsetter.DTO.RegisterRequest;
import com.example.datn_trendsetter.Service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class LogRestController {
    private final AuthService authService;

    public LogRestController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            // Lấy thông tin phản hồi từ service
            AuthResponse response = authService.login(request);

            // Lưu role và token vào session (hoặc có thể dùng localStorage trên client)
            session.setAttribute("role", response.getRole());
            session.setAttribute("token", response.getToken());

            System.out.println("💾 Role đã được lưu vào session: " + session.getAttribute("role"));
            System.out.println("💾 Token đã được lưu vào session: " + session.getAttribute("token"));

            // Trả về response body với token và role để client lưu trữ
            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + response.getToken())  // Thêm Authorization header
                    .body(Map.of(
                            "redirect", response.getRedirectUrl(),
                            "token", response.getToken(),
                            "role", response.getRole()
                    ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }






    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().body("{\"message\": \"Logged out successfully\"}");
    }
}
