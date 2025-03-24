package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Service.NhanVienService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthAPIController {

    private final NhanVienService nhanVienService;
    private final BCryptPasswordEncoder passwordEncoder;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password, HttpSession session, HttpServletResponse response) {
        Optional<NhanVien> nhanVienOpt = nhanVienService.findByEmail(email);

        if (nhanVienOpt.isPresent()) {
            NhanVien nhanVien = nhanVienOpt.get();
            if (passwordEncoder.matches(password, nhanVien.getPassword())) {
                // Tạo một UUID duy nhất cho phiên làm việc
                String sessionId = UUID.randomUUID().toString(); // Tạo UUID ngẫu nhiên

                // Tạo cookie với session ID duy nhất
                Cookie sessionCookie = new Cookie("SESSION_" + sessionId, sessionId);
                sessionCookie.setMaxAge(60 * 60); // Đặt cookie sống 1 giờ
                sessionCookie.setPath("/");  // Đặt phạm vi cookie
                response.addCookie(sessionCookie);  // Thêm cookie vào response

                // Lưu sessionId vào session để sử dụng khi logout
                session.setAttribute("sessionId", sessionId);
                session.setAttribute("user", nhanVien);
                session.setAttribute("role", nhanVien.getVaiTro().toString());

                return ResponseEntity.ok(nhanVien);
            } else {
                return ResponseEntity.badRequest().body("Mật khẩu sai!");
            }
        }
        return ResponseEntity.badRequest().body("Sai email hoặc mật khẩu!");
    }


    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        // Lấy sessionId từ session
        String sessionId = (String) session.getAttribute("sessionId");

        if (sessionId != null) {
            // Hủy session hiện tại
            session.invalidate();  // Hủy session

            // Xóa cookie bằng cách sử dụng sessionId
            Cookie sessionCookie = new Cookie("SESSION_" + sessionId, null);
            sessionCookie.setMaxAge(0);  // Đặt tuổi cookie là 0 để xóa nó
            sessionCookie.setPath("/");  // Đặt phạm vi cookie
            response.addCookie(sessionCookie);  // Xóa cookie trên trình duyệt

            return ResponseEntity.ok("Đăng xuất thành công!");
        }
        return ResponseEntity.status(401).body("Chưa đăng nhập!");
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
