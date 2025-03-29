package com.example.datn_trendsetter.Controller.User;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InfoClientController {
    @GetMapping("/thong-tin")
    public String getInfo() {
        return "User/InfoClient";
    }
}
