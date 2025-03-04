package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Entity.HoaDonChiTiet;
import com.example.datn_trendsetter.Entity.LichSuHoaDon;
import com.example.datn_trendsetter.Repository.HoaDonChiTietRepository;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.Repository.LichSuHoaDonRepository;
import com.example.datn_trendsetter.Repository.NhanVienRepository;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import com.example.datn_trendsetter.Repository.PhuongThucThanhToanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
public class HoaDonController {
    @Autowired
    private HoaDonRepository hoaDonRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @RequestMapping("admin/hoa-don")
    public String hienThiHoaDon(Model model) {
        model.addAttribute("hoaDon", hoaDonRepository.getAllHoaDon());
        model.addAttribute("khachHang", khachHangRepository.findAll());
        model.addAttribute("nhanVien", nhanVienRepository.findAll());
        model.addAttribute("pgg", phieuGiamGiaRepository.findAll());
        model.addAttribute("pttt", phuongThucThanhToanRepository.findAll());
        return "Admin/HoaDon/hien-thi";
    }

    @PostMapping("/hoa-don/delete/{id}")
    private String remove(@PathVariable("id") Integer idMS) {
        hoaDonRepository.deleteById(idMS);
        return "redirect:/admin/hoa-don";
    }


    //    @GetMapping("/hoa-don/detail/{id}")
//    public String detail(@PathVariable("id") Integer id, Model model) {
//        model.addAttribute("hoaDon", hoaDonRepository.findById(id).orElse(null));
//        model.addAttribute("khachHang", khachHangRepository.findAll());
//        model.addAttribute("nhanVien", nhanVienRepository.findAll());
//        model.addAttribute("pgg", phieuGiamGiaRepository.findAll());
//        model.addAttribute("pttt", phuongThucThanhToanRepository.findAll());
//
//        return "admin/order-details"; // Trả về trang JSP chứ không redirect
//    }
    @GetMapping("/hoa-don/detail/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {
        HoaDon hoaDon = hoaDonRepository.findById(id).orElse(null);
        if (hoaDon == null) {
            return "redirect:/hoa-don";
        }

        List<LichSuHoaDon> listLichSuHoaDon = lichSuHoaDonRepository.findByHoaDonId(id);
        Map<String, String> iconMap = Map.of(
                "Đang Xử Lý", "✍️",
                "Chờ Xác Nhận", "⏳",
                "Đã Xác Nhận", "✅",
                "Chờ Vận Chuyển", "📦",
                "Đang Giao Hàng", "🚚",
                "Đã Giao Hàng", "📬",
                "Đã Thanh Toán", "💳",
                "Đã Hoàn Thành", "🎉",
                "Đã Hủy", "❌",
                "Hoàn Trả/Hoàn Tiền", "🔄"
        );
        List<HoaDonChiTiet> hoaDonChiTiet = hoaDonChiTietRepository.findByHoaDonId(id);
        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("listLichSuHoaDon", listLichSuHoaDon);
        model.addAttribute("iconMap", iconMap);
        model.addAttribute("hoaDonChiTiet", hoaDonChiTiet);
        return "admin/order-details";
    }


}
