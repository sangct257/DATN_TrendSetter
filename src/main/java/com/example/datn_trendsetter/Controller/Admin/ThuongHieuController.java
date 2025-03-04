package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.ThuongHieu;
import com.example.datn_trendsetter.Repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        List<ThuongHieu> thuongHieu = thuongHieuRepository.findAll();
        model.addAttribute("thuongHieu", thuongHieu);
        return "Admin/ThuongHieu/hien-thi";
    }

}
