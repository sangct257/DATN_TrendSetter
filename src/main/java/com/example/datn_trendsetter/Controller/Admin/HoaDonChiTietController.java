package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HoaDonChiTietController {
    @RequestMapping("admin/hoa-don-chi-tiet")
    public String HoaDonChiTiet() {
        return "Admin/HoaDonChiTiet/hien-thi";
    }

}
