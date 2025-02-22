package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class ShopController {

    @Autowired
    private ShopService shopService;

    @GetMapping("admin/sell-counter")
    public String sellCounter(@RequestParam(value = "hoaDonId", required = false) Integer hoaDonId,
                              Model model) {
        shopService.getHoaDonAndProducts(hoaDonId, model);
        return "Admin/Shop/dashboard"; // Return to the page where the form is rendered
    }

    @PostMapping("create-hoa-don")
    public String createHoaDon(HoaDon hoaDon,RedirectAttributes redirectAttributes) {
        try {
            shopService.createHoaDon(hoaDon);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo hóa đơn thành công");
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/sell-counter";

    }

    @PostMapping("confirm-payment")
    public String confirmPayment(@RequestParam("hoaDonId") Integer hoaDonId,
                                 RedirectAttributes redirectAttributes) {
        return shopService.confirmPayment(hoaDonId, redirectAttributes);
    }


}

