package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Entity.LichSuHoaDon;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Repository.LichSuHoaDonRepository;
import com.example.datn_trendsetter.Service.LichSuHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class LichSuHoaDonController {

    @Autowired
    private LichSuHoaDonService lichSuHoaDonService;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @GetMapping("admin/order-details")
    public String orderDetails(@RequestParam(value = "hoaDonId") Integer hoaDonId,
                               Model model,
                               @RequestParam(value = "page", defaultValue = "0") Integer page) {
        if (hoaDonId == null) {
            throw new IllegalArgumentException("hoaDonId không được để trống.");
        }

        lichSuHoaDonService.getOrderDetails(hoaDonId, model, page);
        return "Admin/HoaDonChiTiet/order-details";
    }

}
