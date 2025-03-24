package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class NhanVienController {
    @RequestMapping("admin/nhan-vien")
    public String nhanVien() {
        return "admin/NhanVien/hien-thi";
    }
}
