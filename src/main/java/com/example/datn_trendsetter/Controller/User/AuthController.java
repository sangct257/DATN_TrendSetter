package com.example.datn_trendsetter.Controller.User;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/auth")
    public String authPage() {
        return "User/auth";
    }
}
