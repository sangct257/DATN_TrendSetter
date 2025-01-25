package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HoaDonController {
    @RequestMapping("admin/hoa-don")
    public String HoaDon() {
        return "Admin/HoaDon/hien-thi";
    }
}
