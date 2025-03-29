package com.example.datn_trendsetter.Controller.User;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DonHangClientController {
    @GetMapping("don-hang")
    public String getDonHang() {
        return "User/don-hang";
    }

}
