package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PhieuGiamGiaController {

    @RequestMapping("admin/phieu-giam-gia")
    public String PhieuGiamGia() {
        return "Admin/PhieuGiamGia/hien-thi";
    }
}
