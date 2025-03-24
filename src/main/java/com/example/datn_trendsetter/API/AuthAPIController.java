package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Service.NhanVienService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthAPIController {

    private final NhanVienService nhanVienService;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password, HttpSession session) {
        Optional<NhanVien> nhanVienOpt = nhanVienService.findByEmail(email);

        if (nhanVienOpt.isPresent()) {
            NhanVien nhanVien = nhanVienOpt.get();
            if (passwordEncoder.matches(password, nhanVien.getPassword())) {
                session.setAttribute("user", nhanVien);  // Lưu user vào session
                System.out.println("User đã đăng nhập: " + nhanVien.getEmail()); // In ra console để debug
                return ResponseEntity.ok(nhanVien);
            }
        }
        return ResponseEntity.badRequest().body("Sai email hoặc mật khẩu!");
    }


    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); // Xóa session khi đăng xuất
        return ResponseEntity.ok("Đăng xuất thành công!");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("user");

        if (nhanVien != null) {
            return ResponseEntity.ok(nhanVien);
        }
        return ResponseEntity.status(401).body("Chưa đăng nhập!");
    }
}
