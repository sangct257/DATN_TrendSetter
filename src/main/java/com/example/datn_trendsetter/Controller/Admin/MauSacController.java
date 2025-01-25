package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MauSacController {
    @RequestMapping("admin/mau-sac")
    public String MauSac() {
        return "Admin/MauSac/hien-thi";
    }
}
