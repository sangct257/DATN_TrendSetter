package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Repository.HoaDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class HoaDonController {
    @Autowired
    private HoaDonRepository hoaDonRepository;


    @RequestMapping("admin/hoa-don")
    public String hienThiHoaDon(Model model) {
        model.addAttribute("hoaDon", hoaDonRepository.findAll());
        return "Admin/HoaDon/hien-thi";
    }



}
