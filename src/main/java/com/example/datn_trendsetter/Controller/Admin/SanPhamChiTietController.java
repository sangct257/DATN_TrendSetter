package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SanPhamChiTietController {
    @RequestMapping("admin/san-pham-chi-tiet")
    public String SanPhamChiTiet() {
        return "Admin/SanPhamChiTiet/hien-thi";
    }
}
