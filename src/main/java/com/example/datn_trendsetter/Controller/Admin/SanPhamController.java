package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.SanPham;
import com.example.datn_trendsetter.Repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @GetMapping
    public String getSanPhamPage(Model model) {
        model.addAttribute("sanPhamList", sanPhamRepository.findAll());
        return "Admin/SanPham/hien-thi"; // Trả về template HTML
    }

}

