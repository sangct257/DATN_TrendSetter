package com.example.datn_trendsetter.Controller.User;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/") // Định nghĩa URL gốc
public class HomeController {

    @GetMapping("auth/home") // Khi truy cập /auth/home sẽ trả về trang login
    public String showLoginPage(Model model) {
        return "DangNhap"; // Điều hướng đến file templates/DangNhap.html
    }
}
