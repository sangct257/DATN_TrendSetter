package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PhuongThucThanhToanController {

    @RequestMapping("admin/phuong-thuc-thanh-toan")
    public String PhuongThucThanhToan() {
        return "Admin/PhuongThucThanhToan/hien-thi";
    }
}
