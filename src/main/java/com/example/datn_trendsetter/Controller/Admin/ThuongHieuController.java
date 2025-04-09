package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.ThuongHieu;
import com.example.datn_trendsetter.Repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class ThuongHieuController {
    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @RequestMapping("admin/thuong-hieu")
    public String ThuongHieu(Model model) {
        model.addAttribute("thuongHieu", thuongHieuRepository.findByDeleted(false, Sort.by(Sort.Direction.DESC,"id")));
        return "Admin/ThuongHieu/hien-thi";
    }

}
