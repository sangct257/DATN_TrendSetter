package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DiaChiController {

    @RequestMapping("admin/dia-chi")
    public String DiaChi() {
        return "Admin/DiaChi/hien-thi";
    }
}
