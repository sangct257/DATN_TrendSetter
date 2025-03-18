package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import com.example.datn_trendsetter.Service.HoaDonChiTietService;
import com.example.datn_trendsetter.Service.LichSuHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v2")
public class LichSuHoaDonApiController {
    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;
    
    private ResponseEntity<Map<String, Object>> response(String message, boolean success) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> thayDoiTrangThaiHoaDon(Integer hoaDonId, String trangThai, String ghiChu) {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);
        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();
            hoaDon.setTrangThai(trangThai);
            hoaDonRepository.save(hoaDon);
            luuLichSuHoaDon(hoaDon, trangThai, ghiChu);
            return response("Hóa đơn đã được cập nhật trạng thái: " + trangThai, true);
        }
        return response("Hóa đơn không tồn tại!", false);
    }

    private void luuLichSuHoaDon(HoaDon hoaDon, String hanhDong, String ghiChu) {
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setHanhDong(hanhDong);
        lichSu.setKhachHang(hoaDon.getKhachHang());
        lichSu.setNgayTao(LocalDateTime.now());
        lichSu.setNguoiTao(hoaDon.getNguoiTao());
        lichSu.setGhiChu(ghiChu);
        lichSuHoaDonRepository.save(lichSu);
    }

    @PostMapping("/xac-nhan")
    public ResponseEntity<?> xacNhan(@RequestParam("hoaDonId") Integer hoaDonId) {
        return thayDoiTrangThaiHoaDon(hoaDonId,"Đã Xác Nhận" , "Hoá đơn đã xác nhận");
    }

    @PostMapping("/van-chuyen")
    public ResponseEntity<?> vanChuyen(@RequestParam("hoaDonId") Integer hoaDonId) {
        return thayDoiTrangThaiHoaDon(hoaDonId, "Chờ Vận Chuyển", "Hóa đơn đang vận chuyển");
    }

    @PostMapping("/xac-nhan-thanh-toan")
    public ResponseEntity<?> xacNhanThanhToan(@RequestParam("hoaDonId") Integer hoaDonId) {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);
        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();
            hoaDon.setLoaiGiaoDich("Đã Thanh Toán");
            hoaDon.setTrangThai("Đã Thanh Toán");
            hoaDonRepository.save(hoaDon);
            luuLichSuHoaDon(hoaDon, "Đã Thanh Toán", "Hóa đơn đã thanh toán");
            return response("Hóa đơn đã được xác nhận thanh toán!", true);
        }
        return response("Hóa đơn không tồn tại!", false);
    }

    @PostMapping("/xac-nhan-hoan-thanh")
    public ResponseEntity<?> xacNhanHoanThanh(@RequestParam("hoaDonId") Integer hoaDonId) {
        return thayDoiTrangThaiHoaDon(hoaDonId, "Đã Hoàn Thành", "Hóa đơn đã hoàn thành");
    }

    @PostMapping("/quay-lai")
    public ResponseEntity<?> quayLai(@RequestParam("hoaDonId") Integer hoaDonId) {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);
        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();
            hoaDon.setLoaiGiaoDich("Trả Sau");
            hoaDon.setTrangThai("Chờ Xác Nhận");
            hoaDonRepository.save(hoaDon);
            luuLichSuHoaDon(hoaDon, "Chờ Xác Nhận", "Quay lại trạng thái chờ xác nhận");
            return response("Quay lại trạng thái chờ xác nhận!", true);
        }
        return response("Hóa đơn không tồn tại!", false);
    }

    @PostMapping("/huy")
    public ResponseEntity<?> huy(@RequestParam("hoaDonId") Integer hoaDonId) {
        // Lấy danh sách chi tiết hóa đơn theo hoaDonId
        List<HoaDonChiTiet> danhSachChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

        // Tìm hóa đơn theo ID
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        if (danhSachChiTiet.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errorMessage", "Không tìm thấy chi tiết hóa đơn!"));
        }

        // Hoàn trả số lượt sử dụng của phiếu giảm giá nếu hóa đơn có sử dụng
        if (hoaDon.getPhieuGiamGia() != null) {
            PhieuGiamGia phieuGiamGia = hoaDon.getPhieuGiamGia();
            phieuGiamGia.setSoLuotSuDung(phieuGiamGia.getSoLuotSuDung() + 1);
            phieuGiamGiaRepository.save(phieuGiamGia);
        }

        // Hoàn trả lại số lượng sản phẩm
        for (HoaDonChiTiet hoaDonChiTiet : danhSachChiTiet) {
            SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();
            if (sanPhamChiTiet != null) {
                sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() + hoaDonChiTiet.getSoLuong());
                sanPhamChiTietRepository.save(sanPhamChiTiet);

                // Cập nhật số lượng sản phẩm chính
                hoaDonChiTietService.updateStockForProduct(sanPhamChiTiet.getSanPham());
            }
        }

        // Đổi trạng thái hóa đơn thành "Đã Hủy"
        return thayDoiTrangThaiHoaDon(hoaDonId, "Đã Hủy", "Hóa đơn đã bị hủy");
    }


}
