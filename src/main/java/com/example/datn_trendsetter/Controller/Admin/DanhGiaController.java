package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DanhGiaController {

    @RequestMapping("admin/danh-gia")
    public String danhGia() {
        return "Admin/DanhGia/hien-thi";
    }
}
