package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.ResponseMessage;
import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import com.example.datn_trendsetter.Service.HoaDonChiTietService;
import com.example.datn_trendsetter.Service.LichSuHoaDonService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    @Autowired
    private LichSuThanhToanRepository lichSuThanhToanRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    private ResponseEntity<Map<String, Object>> response(String message, boolean success) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> thayDoiTrangThaiHoaDon(Integer hoaDonId, String trangThai, String ghiChu,HttpSession session) throws Exception {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);
        if (optionalHoaDon.isPresent()) {

            // Lấy nhân viên từ session
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("user");
            if (nhanVienSession == null) {
                throw new Exception("Bạn cần đăng nhập để tạo hóa đơn.");
            }


            HoaDon hoaDon = optionalHoaDon.get();
            hoaDon.setTrangThai(trangThai);
            hoaDon.setNhanVien(nhanVienSession);
            hoaDon.setNguoiTao(nhanVienSession.getHoTen());
            hoaDon.setNguoiSua(nhanVienSession.getHoTen());
            hoaDonRepository.save(hoaDon);
            luuLichSuHoaDon(hoaDon, trangThai, ghiChu,session);
            return response("Hóa đơn đã được cập nhật trạng thái: " + trangThai, true);
        }
        return response("Hóa đơn không tồn tại!", false);
    }

    private void luuLichSuHoaDon(HoaDon hoaDon, String hanhDong, String ghiChu,HttpSession session) throws Exception {

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("user");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập để tạo hóa đơn.");
        }

        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setHanhDong(hanhDong);
        lichSu.setKhachHang(hoaDon.getKhachHang());
        lichSu.setNgayTao(LocalDateTime.now());
        lichSu.setNhanVien(nhanVienSession);
        lichSu.setNguoiTao(nhanVienSession.getHoTen());
        lichSu.setNguoiTao(nhanVienSession.getHoTen());
        lichSu.setGhiChu(ghiChu);
        lichSuHoaDonRepository.save(lichSu);
    }

    @PostMapping("/xac-nhan")
    public ResponseEntity<?> xacNhan(@RequestParam("hoaDonId") Integer hoaDonId,HttpSession session) throws Exception {
        try {
            return thayDoiTrangThaiHoaDon(hoaDonId,"Đã Xác Nhận" , "Hoá đơn đã xác nhận",session);
        } catch (Exception e) {
            return response("Lỗi khi xác nhận hóa đơn: " + e.getMessage(), false);
        }
    }

    @PostMapping("/van-chuyen")
    public ResponseEntity<?> vanChuyen(@RequestParam("hoaDonId") Integer hoaDonId,HttpSession session) throws Exception {
        return thayDoiTrangThaiHoaDon(hoaDonId, "Chờ Vận Chuyển", "Hóa đơn đang vận chuyển",session);
    }

    @PostMapping("/giao-hang")
    public ResponseEntity<?> giaoHang(@RequestParam("hoaDonId") Integer hoaDonId,HttpSession session) throws Exception {
        return thayDoiTrangThaiHoaDon(hoaDonId, "Đang Giao Hàng", "Hóa đơn đang giao hàng",session);
    }

    @PostMapping("/xac-nhan-thanh-toan")
    public ResponseEntity<?> xacNhanThanhToan(
            @RequestParam("hoaDonId") Integer hoaDonId,
            @RequestParam("phuongThucThanhToan") Integer phuongThucId,
            HttpSession session) throws Exception{

        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);
        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();
            float tongTienHoaDon = hoaDon.getTongTien();
            float tongTienDaThanhToan = lichSuThanhToanRepository.sumSoTienThanhToanByHoaDon(hoaDonId);

            // Nếu đã thanh toán đủ, chỉ set trạng thái hóa đơn và không làm gì thêm
            if (tongTienDaThanhToan >= tongTienHoaDon) {
                hoaDon.setTrangThai("Đã Thanh Toán");
                hoaDonRepository.save(hoaDon);
                return response("Hóa đơn đã thanh toán đầy đủ, không cần thay đổi gì thêm.", true);
            }


            // Lấy nhân viên từ session
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("user");
            if (nhanVienSession == null) {
                throw new Exception("Bạn cần đăng nhập để tạo hóa đơn.");
            }

            // Nếu chưa thanh toán đủ, xử lý thanh toán phần còn thiếu
            float soTienConThieu = tongTienHoaDon - tongTienDaThanhToan;
            LichSuThanhToan lichSuThanhToan = new LichSuThanhToan();
            lichSuThanhToan.setHoaDon(hoaDon);
            lichSuThanhToan.setSoTienThanhToan(soTienConThieu);
            lichSuThanhToan.setNhanVien(hoaDon.getNhanVien());
            lichSuThanhToan.setThoiGianThanhToan(LocalDateTime.now());
            lichSuThanhToan.setTrangThai("Đã Thanh Toán");
            lichSuThanhToan.setGhiChu("Thanh toán số tiền còn lại " + hoaDon.getMaHoaDon());

            // Lấy phương thức thanh toán
            Optional<PhuongThucThanhToan> optionalPTTT = phuongThucThanhToanRepository.findById(phuongThucId);
            if (optionalPTTT.isPresent()) {
                PhuongThucThanhToan phuongThucThanhToan = optionalPTTT.get();
                lichSuThanhToan.setPhuongThucThanhToan(phuongThucThanhToan);

                // Kiểm tra nếu phương thức thanh toán là "Tiền Mặt"
                if (!"Tiền Mặt".equals(phuongThucThanhToan.getTenPhuongThuc())) {
                    lichSuThanhToan.setMaGiaoDich(generateTransactionCode());
                }
            } else {
                return response("Phương thức thanh toán không hợp lệ!", false);
            }

            lichSuThanhToanRepository.save(lichSuThanhToan);

            // Cập nhật trạng thái hóa đơn và loại giao dịch
            hoaDon.setLoaiGiaoDich(hoaDon.getLoaiGiaoDich());
            hoaDon.setTrangThai("Đã Thanh Toán");
            hoaDonRepository.save(hoaDon);
            luuLichSuHoaDon(hoaDon, "Đã Thanh Toán", "Hóa đơn đã thanh toán",session);

            return response("Hóa đơn đã được xác nhận thanh toán!", true);
        }
        return response("Hóa đơn không tồn tại!", false);
    }


    private String generateTransactionCode() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timePart = LocalDateTime.now().format(formatter);
        int randomPart = new Random().nextInt(900000) + 100000; // Số ngẫu nhiên 6 chữ số
        return "GD" + timePart + randomPart; // VD: GD20240321123045123456
    }


    @PostMapping("/xac-nhan-hoan-thanh")
    public ResponseEntity<?> xacNhanHoanThanh(@RequestParam("hoaDonId") Integer hoaDonId,HttpSession session) throws Exception {
        // Lấy hóa đơn từ cơ sở dữ liệu
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);

        if (hoaDon == null) {
            return ResponseEntity.badRequest().body(new ResponseMessage("error", "Hóa đơn không tồn tại."));
        }

        // Tính tổng số tiền thanh toán từ lịch sử thanh toán
        double tongTienThanhToan = tinhTongTienThanhToan(hoaDonId);

        // Kiểm tra nếu tổng số tiền thanh toán không bằng tổng tiền hóa đơn
        if (tongTienThanhToan != hoaDon.getTongTien()) {
            return ResponseEntity.badRequest().body(new ResponseMessage("error", "Vui lòng xác nhận thanh toán."));
        }

        // Xác nhận hóa đơn đã hoàn thành
        return thayDoiTrangThaiHoaDon(hoaDonId, "Đã Hoàn Thành", "Hóa đơn đã hoàn thành",session);
    }


    // Thêm phương thức tính tổng số tiền thanh toán từ lịch sử thanh toán
    private double tinhTongTienThanhToan(Integer hoaDonId) {
        List<LichSuThanhToan> lichSuThanhToanList = lichSuThanhToanRepository.findByHoaDonId(hoaDonId);

        // Tính tổng tiền thanh toán từ lịch sử thanh toán và trả về kiểu double
        return lichSuThanhToanList.stream()
                .mapToDouble(LichSuThanhToan::getSoTienThanhToan)
                .sum();
    }


    @PostMapping("/quay-lai")
    public ResponseEntity<?> quayLai(@RequestParam("hoaDonId") Integer hoaDonId,HttpSession session) throws Exception {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);
        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();
            hoaDon.setLoaiGiaoDich("Trả Sau");
            hoaDon.setTrangThai("Chờ Xác Nhận");
            hoaDonRepository.save(hoaDon);
            luuLichSuHoaDon(hoaDon, "Chờ Xác Nhận", "Quay lại trạng thái chờ xác nhận",session);
            return response("Quay lại trạng thái chờ xác nhận!", true);
        }
        return response("Hóa đơn không tồn tại!", false);
    }

    @PostMapping("/huy")
    public ResponseEntity<?> huy(@RequestParam("hoaDonId") Integer hoaDonId,HttpSession session) throws Exception {
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
        return thayDoiTrangThaiHoaDon(hoaDonId, "Đã Hủy", "Hóa đơn đã bị hủy",session);
    }


}
