package com.example.datn_trendsetter.Controller.User;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class ChiTietSPControler {
    @GetMapping("chi-tiet-san-pham")
    public String chiTietSanPham() {
        return "User/detail";
    }
}
