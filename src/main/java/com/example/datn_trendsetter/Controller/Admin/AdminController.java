package com.example.datn_trendsetter.Controller.Admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @GetMapping
    public String index() {
        return "redirect:/admin/";
    }
    @GetMapping("/")
    public String adminDashboard(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");

        if (role == null) {
            return "redirect:/auth/home";

        }

        System.out.println("✅ Role hiện tại: " + role);

        model.addAttribute("role", role);
        return "Admin/index";
    }

}
