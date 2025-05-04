package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class NhanVienController {
    @Autowired
    private NhanVienRepository nhanVienRepository;

    @RequestMapping("admin/nhan-vien")
    public String danhMuc(Model model) {
        model.addAttribute("listNhanVien", nhanVienRepository.findByDeleted(false, Sort.by(Sort.Direction.DESC, "id")));
        return "Admin/NhanVien/hien-thi";    }

}
