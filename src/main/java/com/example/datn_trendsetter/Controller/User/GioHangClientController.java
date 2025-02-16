package com.example.datn_trendsetter.Controller.User;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class GioHangClientController {
    @GetMapping("gio-hang")
    public String gioiThanhToan() {
        return "User/cart";
    }
}
