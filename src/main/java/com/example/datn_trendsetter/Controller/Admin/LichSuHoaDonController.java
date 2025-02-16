package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Entity.LichSuHoaDon;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Repository.LichSuHoaDonRepository;
import com.example.datn_trendsetter.Service.HoaDonService;
import com.example.datn_trendsetter.Service.LichSuHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
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
        return "Admin/order-details";
    }

    @PostMapping("xac-nhan")
    public String xacNhan(@RequestParam("hoaDonId") Integer hoaDonId, RedirectAttributes redirectAttributes) {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);

        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();

            // Cập nhật trạng thái hóa đơn
            hoaDon.setTrangThai("Đã Xác Nhận");
            hoaDonRepository.save(hoaDon);

            // Thêm lịch sử hóa đơn
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hoaDon);
            lichSu.setHanhDong("Đã Xác Nhận");
            lichSu.setNgayTao(LocalDateTime.now());
            lichSu.setNguoiTao(hoaDon.getNguoiTao()); // Hoặc lấy từ session
            lichSu.setGhiChu("Đã Xác Nhận Hóa Đơn : " +hoaDon.getMaHoaDon());
            lichSuHoaDonRepository.save(lichSu);


            redirectAttributes.addFlashAttribute("successMessage", "Hóa đơn đã được xác nhận!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại!");
        }

        return "redirect:/admin/order-details?hoaDonId=" + hoaDonId;
    }


    @PostMapping("van-chuyen")
    public String vanChuyen(@RequestParam("hoaDonId") Integer hoaDonId, RedirectAttributes redirectAttributes) {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);

        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();

            // Cập nhật trạng thái hóa đơn
            hoaDon.setTrangThai("Chờ Vận Chuyển");
            hoaDonRepository.save(hoaDon);

            // Thêm lịch sử hóa đơn
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hoaDon);
            lichSu.setHanhDong("Chờ Vận Chuyển");
            lichSu.setNgayTao(LocalDateTime.now());
            lichSu.setNguoiTao(hoaDon.getNguoiTao()); // Hoặc lấy từ session
            lichSu.setGhiChu("Hóa đơn : " +hoaDon.getMaHoaDon()+" đang vận chuyển");

            lichSuHoaDonRepository.save(lichSu);

            redirectAttributes.addFlashAttribute("successMessage", "Hóa đơn đang chờ vận chuyển!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại!");
        }

        return "redirect:/admin/order-details?hoaDonId=" + hoaDonId;
    }

    @PostMapping("giao-hang")
    public String giaoHang(@RequestParam("hoaDonId") Integer hoaDonId, RedirectAttributes redirectAttributes) {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);

        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();

            // Cập nhật trạng thái hóa đơn
            hoaDon.setTrangThai("Đang Giao Hàng");
            hoaDonRepository.save(hoaDon);

            // Thêm lịch sử hóa đơn
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hoaDon);
            lichSu.setHanhDong("Đang Giao Hàng");
            lichSu.setNgayTao(LocalDateTime.now());
            lichSu.setNguoiTao(hoaDon.getNguoiTao()); // Hoặc lấy từ session
            lichSu.setGhiChu("Hóa đơn : " +hoaDon.getMaHoaDon() + " đang giao hàng");
            lichSuHoaDonRepository.save(lichSu);


            redirectAttributes.addFlashAttribute("successMessage", "Hóa đơn đã được xác nhận giao hàng!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại!");
        }

        return "redirect:/admin/order-details?hoaDonId=" + hoaDonId;
    }

    @PostMapping("xac-nhan-giao-hang")
    public String xacNhanGiaoHang(@RequestParam("hoaDonId") Integer hoaDonId, RedirectAttributes redirectAttributes) {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);

        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();

            // Cập nhật trạng thái hóa đơn
            hoaDon.setTrangThai("Đã Giao Hàng");
            hoaDonRepository.save(hoaDon);

            // Thêm lịch sử hóa đơn
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hoaDon);
            lichSu.setHanhDong("Đã Giao Hàng");
            lichSu.setNgayTao(LocalDateTime.now());
            lichSu.setNguoiTao(hoaDon.getNguoiTao()); // Hoặc lấy từ session
            lichSu.setGhiChu("Hóa đơn : " +hoaDon.getMaHoaDon() + " đã giao hàng");
            lichSuHoaDonRepository.save(lichSu);


            redirectAttributes.addFlashAttribute("successMessage", "Hóa đơn đã được xác nhận đã giao hàng!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại!");
        }

        return "redirect:/admin/order-details?hoaDonId=" + hoaDonId;
    }

    @PostMapping("xac-nhan-thanh-toan")
    public String xacNhanThanhToan(@RequestParam("hoaDonId") Integer hoaDonId, RedirectAttributes redirectAttributes) {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);

        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();

            // Cập nhật trạng thái hóa đơn
            hoaDon.setLoaiGiaoDich("Đã Thanh Toán");
            hoaDon.setTrangThai("Đã Thanh Toán");
            hoaDonRepository.save(hoaDon);

            // Thêm lịch sử hóa đơn
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hoaDon);
            lichSu.setHanhDong("Đã Thanh Toán");
            lichSu.setNgayTao(LocalDateTime.now());
            lichSu.setNguoiTao(hoaDon.getNguoiTao()); // Hoặc lấy từ session
            lichSu.setGhiChu("Hóa đơn : " +hoaDon.getMaHoaDon() + " đã thanh toán");
            lichSuHoaDonRepository.save(lichSu);


            redirectAttributes.addFlashAttribute("successMessage", "Hóa đơn đã được xác nhận đã thanh toán!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại!");
        }

        return "redirect:/admin/order-details?hoaDonId=" + hoaDonId;
    }

    @PostMapping("xac-nhan-hoan-thanh")
    public String xacNhanHoanThanh(@RequestParam("hoaDonId") Integer hoaDonId, RedirectAttributes redirectAttributes) {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);

        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();

            // Cập nhật trạng thái hóa đơn
            hoaDon.setTrangThai("Đã Hoàn Thành");
            hoaDonRepository.save(hoaDon);

            // Thêm lịch sử hóa đơn
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hoaDon);
            lichSu.setHanhDong("Đã Hoàn Thành");
            lichSu.setNgayTao(LocalDateTime.now());
            lichSu.setNguoiTao(hoaDon.getNguoiTao()); // Hoặc lấy từ session
            lichSu.setGhiChu("Hóa đơn : " +hoaDon.getMaHoaDon() + " đã hoàn thành");
            lichSuHoaDonRepository.save(lichSu);


            redirectAttributes.addFlashAttribute("successMessage", "Hóa đơn đã được xác nhận đã hoàn thành!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại!");
        }

        return "redirect:/admin/order-details?hoaDonId=" + hoaDonId;
    }

    @PostMapping("quay-lai")
    public String quayLai(@RequestParam("hoaDonId") Integer hoaDonId, RedirectAttributes redirectAttributes) {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);

        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();

            // Cập nhật trạng thái hóa đơn
            hoaDon.setLoaiGiaoDich("Trả Sau");
            hoaDon.setTrangThai("Chờ Xác Nhận");
            hoaDonRepository.save(hoaDon);

            // Thêm lịch sử hóa đơn
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hoaDon);
            lichSu.setHanhDong("Chờ Xác Nhận");
            lichSu.setNgayTao(LocalDateTime.now());
            lichSu.setNguoiTao(hoaDon.getNguoiTao()); // Hoặc lấy từ session
            lichSu.setGhiChu(" Quay lại trạng thái hóa đơn : " +hoaDon.getMaHoaDon() + " chờ xác nhận");
            lichSuHoaDonRepository.save(lichSu);


            redirectAttributes.addFlashAttribute("successMessage", "Quay lại trạng thái chờ xác nhận!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại!");
        }

        return "redirect:/admin/order-details?hoaDonId=" + hoaDonId;
    }

    @PostMapping("huy")
    public String huy(@RequestParam("hoaDonId") Integer hoaDonId, RedirectAttributes redirectAttributes) {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);

        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();

            // Cập nhật trạng thái hóa đơn
            hoaDon.setTrangThai("Đã Hủy");
            hoaDonRepository.save(hoaDon);

            // Thêm lịch sử hóa đơn
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hoaDon);
            lichSu.setHanhDong("Đã Hủy");
            lichSu.setNgayTao(LocalDateTime.now());
            lichSu.setNguoiTao(hoaDon.getNguoiTao()); // Hoặc lấy từ session
            lichSu.setGhiChu("Hóa đơn : " +hoaDon.getMaHoaDon() + "đã bị hủy");
            lichSuHoaDonRepository.save(lichSu);


            redirectAttributes.addFlashAttribute("successMessage", "Hóa đơn đã bị hủy!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại!");
        }

        return "redirect:/admin/order-details?hoaDonId=" + hoaDonId;
    }

}
