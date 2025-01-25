package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ThuongHieuController {
    @RequestMapping("admin/thuong-hieu")
    public String ThuongHieu() {
        return "Admin/ThuongHieu/hien-thi";
    }
}
