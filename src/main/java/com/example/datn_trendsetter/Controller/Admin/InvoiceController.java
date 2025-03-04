package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin/invoice")
public class InvoiceController {
    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    // Hiển thị hóa đơn
    @GetMapping
    public String viewInvoice(@RequestParam("hoaDonId") Integer hoaDonId, Model model) {
        if (hoaDonId != null) {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);

            if (hoaDon != null) {
                // Lấy danh sách chi tiết hóa đơn
                List<HoaDonChiTiet> hoaDonChiTietList = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

                // Tính tổng số sản phẩm trong hóa đơn
                int tongSanPham = hoaDonChiTietList.stream()
                        .mapToInt(HoaDonChiTiet::getSoLuong)
                        .sum();
                hoaDon.setTongSanPham(tongSanPham);

                // Đưa dữ liệu vào model
                model.addAttribute("hoaDonChiTiet", hoaDonChiTietList);
                model.addAttribute("hoaDon", hoaDon);
            }
        }
        return "Admin/Shop/invoice";
    }


    // Xác nhận hóa đơn
    @PostMapping("/confirm")
    public String confirmInvoice(@RequestParam("hoaDonId") Integer hoaDonId, RedirectAttributes redirectAttributes) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));
        hoaDon.setTrangThai("Đã Hoàn Thành");
        hoaDonRepository.save(hoaDon);
        redirectAttributes.addFlashAttribute("successMessage", "Hóa đơn đã được xác nhận!");
        return "redirect:/admin/sell-counter";
    }

    // Hủy hóa đơn
    @GetMapping("/cancel")
    public String cancelInvoice(@RequestParam("hoaDonId") Integer hoaDonId, RedirectAttributes redirectAttributes) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));
        hoaDon.setTrangThai("Đã Hủy");
        hoaDonRepository.save(hoaDon);
        redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn đã bị hủy!");
        return "redirect:/admin/sell-counter";
    }

}
