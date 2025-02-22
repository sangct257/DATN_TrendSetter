package com.example.datn_trendsetter.Controller.User;

import com.example.datn_trendsetter.DTO.AuthResponse;
import com.example.datn_trendsetter.DTO.LoginRequest;
import com.example.datn_trendsetter.DTO.RegisterRequest;
import com.example.datn_trendsetter.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class DangNhapController {
    private final AuthService authService;

    public DangNhapController(AuthService authService) {
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok().body("{\"redirect\": \"/trang-chu\", \"token\": \"" + response.getToken() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().body("{\"message\": \"Logged out successfully\"}");
    }
}
