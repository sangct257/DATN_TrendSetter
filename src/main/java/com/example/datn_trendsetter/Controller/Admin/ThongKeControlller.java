package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ThongKeControlller {
    @RequestMapping("admin/thong-ke")
    public String HoaDonChiTiet() {
        return "Admin/ThongKe/hien-thi";
    }
}
