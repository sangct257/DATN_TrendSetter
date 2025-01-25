package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DanhMucController {
    @RequestMapping("admin/danh-muc")
    public String danhMuc() {
        return "Admin/DanhMuc/hien-thi";
    }

    @RequestMapping("admin/addDanhMuc")
    public String addDanhMuc() {
        return "Admin/DanhMuc/add";
    }
}
