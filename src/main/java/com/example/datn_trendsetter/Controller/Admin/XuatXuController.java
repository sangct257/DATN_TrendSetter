package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class XuatXuController {
    @RequestMapping("admin/xuat-xu")
    public String XuatXu() {
        return "Admin/XuatXu/hien-thi";
    }
}
