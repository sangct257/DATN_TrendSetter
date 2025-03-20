package com.example.datn_trendsetter.Controller.User;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/") // Định nghĩa URL gốc
public class LogController {

    @GetMapping("auth/home")
    public String showLoginPage(Model model) {
        return "DangNhap/DangNhapKhach";
    }    @GetMapping("auth/trendsetter")
    public String showLogin(Model model) {
        return "DangNhap/DangNhapNV";
    }
}
