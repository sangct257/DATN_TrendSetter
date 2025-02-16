package com.example.datn_trendsetter.Controller.User;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class ThanhToanController {
    @GetMapping("thanh-toan")
    public String thanhToan() {
        return "User/checkout";
    }
}
