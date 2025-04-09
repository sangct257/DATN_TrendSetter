package com.example.datn_trendsetter.Controller.User;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InfoClientController {
    @GetMapping("/thong-tin")
    public String getInfo(HttpSession session) {
        // Kiểm tra xem session có chứa thông tin KhachHang hay không
        if (session.getAttribute("userKhachHang") == null) {
            // Nếu chưa đăng nhập, chuyển hướng về trang chủ
            return "redirect:/trang-chu";  // Đảm bảo sử dụng 'redirect:' đúng cách
        }
        return "User/InfoClient";
    }
}
