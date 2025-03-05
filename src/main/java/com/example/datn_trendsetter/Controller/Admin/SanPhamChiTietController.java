package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.SanPhamChiTiet;
import com.example.datn_trendsetter.Repository.HinhAnhRepository;
import com.example.datn_trendsetter.Repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SanPhamChiTietController {
    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;


    @RequestMapping("admin/san-pham-chi-tiet")
    public String SanPhamChiTiet(@RequestParam(value = "sanPhamChiTietId", required = false, defaultValue = "0") Integer sanPhamChiTietId,
                                 Model model) {
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(sanPhamChiTietId).orElse(null);
        model.addAttribute("sanPhamChiTiet", sanPhamChiTiet);
        return "Admin/SanPhamChiTiet/hien-thi";
    }
}
