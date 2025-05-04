package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class KhachHangController {
    @Autowired
    private KhachHangRepository khachHangRepository;

    @RequestMapping("admin/khach-hang")
    public String danhMuc(Model model) {
        model.addAttribute("listKhachHang", khachHangRepository.findByDeleted(false, Sort.by(Sort.Direction.DESC, "id")));
        return "Admin/KhachHang/hien-thi";
    }
}
