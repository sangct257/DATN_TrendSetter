package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DotGiamGiaController {
    @RequestMapping("admin/dot-giam-gia")
    public String DotGiamGia() {
        return "Admin/DotGiamGia/hien-thi";
    }

    @RequestMapping("admin/dot-giam-gia/dot-giam-gia-chi-tiet")
    public String DotGiamGiaChiTiet() {
        return "Admin/DotGiamGia/dot-giam-gia-chi-tiet";
    }
}
