package com.example.datn_trendsetter.Controller.User;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DiaChiClientController {
    @GetMapping("/dia-chi")
    public String getInfo() {
        return "User/DiaChiClient";
    }
}
