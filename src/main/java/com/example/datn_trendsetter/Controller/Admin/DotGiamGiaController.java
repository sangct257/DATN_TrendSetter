package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.DotGiamGia;
import com.example.datn_trendsetter.Repository.DotGiamGiaRepository;
import com.example.datn_trendsetter.Repository.SanPhamChiTietRepository;
import com.example.datn_trendsetter.Repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
public class DotGiamGiaController {
    @Autowired
    private DotGiamGiaRepository dotGiamGiaRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @RequestMapping("admin/dot-giam-gia")
    public String DotGiamGia(Model model) {
        model.addAttribute("dotGiamGia", dotGiamGiaRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "Admin/DotGiamGia/hien-thi";
    }

    @RequestMapping("admin/add-dot-giam-gia")
    public String AddDotGiamGia(Model model) {
        model.addAttribute("sanPhamChiTiet", sanPhamChiTietRepository.findByTrangThai("Còn Hàng",Sort.by(Sort.Direction.DESC, "id")));
        return "Admin/DotGiamGia/add-dot-giam-gia";
    }
    @RequestMapping("admin/dot-giam-gia-chi-tiet")
    public String DotGiamGiaChiTiet() {
        return "Admin/DotGiamGia/dot-giam-gia-chi-tiet";
    }
}
