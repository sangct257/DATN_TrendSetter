package com.example.datn_trendsetter.Controller.User;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SanPhamClientController {
    @GetMapping("san-pham")
    public String sanPham() {
        return "User/sanPhamClient";
    }
}
