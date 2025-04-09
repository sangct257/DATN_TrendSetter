package com.example.datn_trendsetter.Controller.User;

import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Repository.LichSuHoaDonRepository;
import com.example.datn_trendsetter.Service.LichSuHoaDonService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DonHangClientController {
    @Autowired
    private LichSuHoaDonService lichSuHoaDonService;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @GetMapping("don-hang")
    public String getDonHang(@RequestParam(value = "maHoaDon", required = false) String maHoaDon,
                             Model model) {
        if (maHoaDon != null && !maHoaDon.trim().isEmpty()) {
            lichSuHoaDonService.getHoaDon(maHoaDon, model);
        }
        return "User/don-hang";
    }

    @GetMapping("list")
    public String donHang(HttpSession session) {
        // Kiểm tra xem session có chứa thông tin KhachHang hay không
        if (session.getAttribute("userKhachHang") == null) {
            // Nếu chưa đăng nhập, chuyển hướng về trang chủ
            return "redirect:/trang-chu";  // Đảm bảo sử dụng 'redirect:' đúng cách
        }

        // Nếu đã đăng nhập, trả về trang DonHangClient
        return "User/DonHangClient";
    }


}
