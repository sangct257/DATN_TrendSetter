package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Repository.XuatXuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class XuatXuController {
    @Autowired
    private XuatXuRepository xuatXuRepository;

    @RequestMapping("admin/xuat-xu")
    public String XuatXu(Model model) {
        model.addAttribute("xuatXu", xuatXuRepository.findByDeleted(false, Sort.by(Sort.Direction.DESC,"id")));
        return "Admin/XuatXu/hien-thi";
    }
}
