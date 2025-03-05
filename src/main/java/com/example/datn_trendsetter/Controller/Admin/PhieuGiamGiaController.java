package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import com.example.datn_trendsetter.Service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PhieuGiamGiaController {

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @RequestMapping("admin/phieu-giam-gia")
    public String PhieuGiamGia(Model model) {
        model.addAttribute("phieuGiamGia", phieuGiamGiaRepository.findAll());
        return "Admin/PhieuGiamGia/hien-thi";
    }
}
