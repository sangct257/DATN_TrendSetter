package com.example.datn_trendsetter.Controller.User;

import com.example.datn_trendsetter.Repository.LichSuHoaDonRepository;
import com.example.datn_trendsetter.Service.LichSuHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DonHangClientController {
    @Autowired
    private LichSuHoaDonService lichSuHoaDonService;

    @GetMapping("don-hang")
    public String getDonHang(@RequestParam(value = "maHoaDon", required = false) String maHoaDon,
                             Model model) {
        if (maHoaDon != null && !maHoaDon.trim().isEmpty()) {
            lichSuHoaDonService.getHoaDon(maHoaDon, model);
        }
        return "User/don-hang";
    }


}
