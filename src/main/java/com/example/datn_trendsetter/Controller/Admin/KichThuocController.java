package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class KichThuocController {
    @RequestMapping("admin/kich-thuoc")
    public String KichThuoc() {
        return "Admin/KichThuoc/hien-thi";
    }
}
