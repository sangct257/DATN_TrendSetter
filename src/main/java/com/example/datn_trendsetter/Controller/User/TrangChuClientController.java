package com.example.datn_trendsetter.Controller.User;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrangChuClientController {
    @GetMapping("trang-chu")
    public String trangChu() {
        return "User/trangChuClient";
    }

}
