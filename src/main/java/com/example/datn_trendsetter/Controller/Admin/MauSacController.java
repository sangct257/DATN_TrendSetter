package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Repository.MauSacRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MauSacController {
    @Autowired
    private MauSacRepository mauSacRepository;
    @RequestMapping("admin/mau-sac")
    public String MauSac(Model model) {
        model.addAttribute("mauSac", mauSacRepository.findAll());
        return "Admin/MauSac/hien-thi";
    }
}
