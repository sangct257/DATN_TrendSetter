package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Repository.DanhMucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DanhMucController {
    @Autowired
    private DanhMucRepository danhMucRepository;
    @RequestMapping("admin/danh-muc")
    public String danhMuc(Model model) {
        model.addAttribute("danhMuc", danhMucRepository.findAll());
        return "Admin/DanhMuc/hien-thi";
    }

}
