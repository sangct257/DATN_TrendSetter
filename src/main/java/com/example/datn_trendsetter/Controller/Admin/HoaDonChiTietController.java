package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HoaDonChiTietController {
    @RequestMapping("admin/hoa-don-chi-tiet")
    public String HoaDonChiTiet() {
        return "Admin/HoaDonChiTiet/hien-thi";
    }
}
