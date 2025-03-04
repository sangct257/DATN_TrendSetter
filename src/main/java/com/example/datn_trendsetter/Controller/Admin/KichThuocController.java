package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.KichThuoc;
import com.example.datn_trendsetter.Repository.KichThuocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class KichThuocController {
    @Autowired
    private KichThuocRepository kichThuocRepository;

    @RequestMapping("admin/kich-thuoc")
    public String KichThuoc(Model model) {
        model.addAttribute("kichThuoc", kichThuocRepository.findAll());
        return "Admin/KichThuoc/hien-thi";
    }
}
