package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SanPhamController {
    @RequestMapping("admin/san-pham")
    public String SanPham() {
        return "Admin/SanPham/hien-thi";
    }
}
