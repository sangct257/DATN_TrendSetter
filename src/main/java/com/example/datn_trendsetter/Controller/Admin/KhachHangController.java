package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class KhachHangController {
    @RequestMapping("admin/khach-hang")
    public String khachHang() {
        return "admin/KhachHang/hien-thi";
    }

}
