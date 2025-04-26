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
    private LichSuThanhToanRepository lichSuThanhToanRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    private ResponseEntity<Map<String, Object>> response(String message, boolean success) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/xac-nhan")
    public ResponseEntity<?> xacNhan(@RequestParam("hoaDonId") Integer hoaDonId,
                                     @RequestParam("ghiChu") String ghiChu,
                                     HttpSession session) throws Exception {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);
        if (optionalHoaDon.isEmpty()) {
            return response("Hóa đơn không tồn tại!", false);
        }

        HoaDon hoaDon = optionalHoaDon.get();

        // Xử lý trừ số lượng trước khi thay đổi trạng thái
        ResponseEntity<?> truSoLuongResult = xuLyTruSanPhamVaPhieu(hoaDon);
        if (truSoLuongResult != null) {
            return truSoLuongResult; // nếu có lỗi -> trả về
        }

        // Sau khi trừ xong -> cập nhật trạng thái và ghi chú nhập tay
        return thayDoiTrangThaiHoaDon(hoaDonId, "Đã Xác Nhận", ghiChu, session);
    }

    private ResponseEntity<?> thayDoiTrangThaiHoaDon(Integer hoaDonId, String trangThai, String ghiChu, HttpSession session) throws Exception {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);
        if (optionalHoaDon.isPresent()) {

            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            KhachHang khachHangSession = (KhachHang) session.getAttribute("userKhachHang");

            if (nhanVienSession == null && khachHangSession == null) {
                throw new Exception("Bạn cần đăng nhập.");
            }

            HoaDon hoaDon = optionalHoaDon.get();
            hoaDon.setTrangThai(trangThai);

            if (nhanVienSession != null) {
                hoaDon.setNhanVien(nhanVienSession);
                hoaDon.setNguoiTao(nhanVienSession.getHoTen());
                hoaDon.setNguoiSua(nhanVienSession.getHoTen());
            } else {
                hoaDon.setNguoiTao(khachHangSession.getHoTen());
                hoaDon.setNguoiSua(khachHangSession.getHoTen());
            }

            hoaDonRepository.save(hoaDon);
            luuLichSuHoaDon(hoaDon, trangThai, ghiChu, session);

            return response("Hóa đơn đã được cập nhật trạng thái: " + trangThai, true);
        }
        return response("Hóa đơn không tồn tại!", false);
    }

    private void luuLichSuHoaDon(HoaDon hoaDon, String hanhDong, String ghiChu, HttpSession session) throws Exception {
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
        KhachHang khachHangSession = (KhachHang) session.getAttribute("userKhachHang");

        if (nhanVienSession == null && khachHangSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
        }

        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setHanhDong(hanhDong);
        lichSu.setKhachHang(hoaDon.getKhachHang());
        lichSu.setGhiChu(ghiChu);
        lichSu.setNgayTao(LocalDateTime.now());

        if (nhanVienSession != null) {
            lichSu.setNhanVien(nhanVienSession);
            lichSu.setNguoiTao(nhanVienSession.getHoTen());
        } else {
            lichSu.setNguoiTao(khachHangSession.getHoTen());
        }

        lichSuHoaDonRepository.save(lichSu);
    }

    private ResponseEntity<?> xuLyTruSanPhamVaPhieu(HoaDon hoaDon) {
        for (HoaDonChiTiet cthd : hoaDon.getHoaDonChiTiet()) {
            SanPhamChiTiet sanPhamChiTiet = cthd.getSanPhamChiTiet();

            if (sanPhamChiTiet.getSoLuong() < cthd.getSoLuong()) {
                return response("Sản Phẩm Hết Hàng", false);
            }

            sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() - cthd.getSoLuong());
            sanPhamChiTiet.setTrangThai(sanPhamChiTiet.getSoLuong() > 0 ? "Còn Hàng" : "Hết Hàng");
            sanPhamChiTietRepository.save(sanPhamChiTiet);

            SanPham sanPham = sanPhamChiTiet.getSanPham();
            sanPham.setSoLuong(sanPham.getSoLuong() - cthd.getSoLuong());
            sanPham.setTrangThai(sanPham.getSoLuong() > 0 ? "Đang Hoạt Động" : "Ngừng Hoạt Động");
            sanPhamRepository.save(sanPham);
        }

        if (hoaDon.getPhieuGiamGia() != null) {
            PhieuGiamGia phieu = hoaDon.getPhieuGiamGia();

            if (phieu.getSoLuotSuDung() <= 0) {
                phieu.setTrangThai("Ngừng Hoạt Động");
                phieuGiamGiaRepository.save(phieu);
                return response("Phiếu giảm giá đã hết lượt sử dụng", false);
            }

            phieu.setSoLuotSuDung(phieu.getSoLuotSuDung() - 1);
            phieu.setTrangThai(phieu.getSoLuotSuDung() > 0 ? "Đang Hoạt Động" : "Ngừng Hoạt Động");
            phieuGiamGiaRepository.save(phieu);
        }

        return null; // không lỗi
    }

    @PostMapping("/van-chuyen")
    public ResponseEntity<?> vanChuyen(@RequestParam("hoaDonId") Integer hoaDonId,
                                       @RequestParam("ghiChu") String ghiChu,
                                       HttpSession session) throws Exception {
        return thayDoiTrangThaiHoaDon(hoaDonId, "Chờ Vận Chuyển", ghiChu,session);
    }

    @PostMapping("/giao-hang")
    public ResponseEntity<?> giaoHang(@RequestParam("hoaDonId") Integer hoaDonId,
                                      @RequestParam("ghiChu") String ghiChu,
                                      HttpSession session) throws Exception {
        return thayDoiTrangThaiHoaDon(hoaDonId, "Đang Giao Hàng", ghiChu,session);
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
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                throw new Exception("Bạn cần đăng nhập.");
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
    public ResponseEntity<?> xacNhanHoanThanh(@RequestParam("hoaDonId") Integer hoaDonId,
                                              @RequestParam("ghiChu") String ghiChu,
                                              HttpSession session) throws Exception {
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
        return thayDoiTrangThaiHoaDon(hoaDonId, "Đã Hoàn Thành", ghiChu,session);
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
    public ResponseEntity<?> quayLai(@RequestParam("hoaDonId") Integer hoaDonId,
                                     @RequestParam("ghiChu") String ghiChu,
                                     HttpSession session) throws Exception {
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);
        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();

            // ❌ Ngăn không cho quay lại nếu đã đúng trạng thái theo loại
            if (("Online".equalsIgnoreCase(hoaDon.getLoaiHoaDon()) && "Chờ Xác Nhận".equalsIgnoreCase(hoaDon.getTrangThai())) ||
                    ("Giao Hàng".equalsIgnoreCase(hoaDon.getLoaiHoaDon()) && "Đã Xác Nhận".equalsIgnoreCase(hoaDon.getTrangThai()))) {
                return response("Không thể quay lại vì hóa đơn đã ở trạng thái hợp lệ!", false);
            }

            // ✅ Nếu là Online thì hoàn lại số lượng đã trừ
            if ("Online".equalsIgnoreCase(hoaDon.getLoaiHoaDon())) {
                for (HoaDonChiTiet cthd : hoaDon.getHoaDonChiTiet()) {
                    SanPhamChiTiet ctsp = cthd.getSanPhamChiTiet();
                    ctsp.setSoLuong(ctsp.getSoLuong() + cthd.getSoLuong());

                    ctsp.setTrangThai(ctsp.getSoLuong() > 0 ? "Còn Hàng" : "Hết Hàng");
                    sanPhamChiTietRepository.save(ctsp);

                    SanPham sp = ctsp.getSanPham();
                    sp.setSoLuong(sp.getSoLuong() + cthd.getSoLuong());
                    sp.setTrangThai(sp.getSoLuong() > 0 ? "Đang Hoạt Động" : "Không Hoạt Động");
                    sanPhamRepository.save(sp);
                }
            }

            // ✅ Cập nhật trạng thái hóa đơn
            hoaDon.setLoaiGiaoDich(hoaDon.getLoaiGiaoDich());
            hoaDon.setTrangThai("Chờ Xác Nhận");
            hoaDonRepository.save(hoaDon);

            // ✅ Ghi lại lịch sử theo từng loại hóa đơn
            String hanhDongLichSu = "Giao Hàng".equalsIgnoreCase(hoaDon.getLoaiHoaDon())
                    ? "Đã Xác Nhận"
                    : "Chờ Xác Nhận";

            luuLichSuHoaDon(hoaDon, hanhDongLichSu, ghiChu, session);

            return response("Quay lại trạng thái chờ xác nhận!", true);
        }
        return response("Hóa đơn không tồn tại!", false);
    }

    @PostMapping("/huy")
    public ResponseEntity<?> huy(@RequestParam("hoaDonId") Integer hoaDonId,
                                 @RequestParam("ghiChu") String ghiChu,
                                 HttpSession session) throws Exception {
        // Lấy người dùng từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
        KhachHang khachHangSession = (KhachHang) session.getAttribute("userKhachHang");

        if (nhanVienSession == null && khachHangSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
        }

        // Lấy hóa đơn
        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);
        if (optionalHoaDon.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errorMessage", "Không tìm thấy hóa đơn!"));
        }
        HoaDon hoaDon = optionalHoaDon.get();

        // Lấy chi tiết hóa đơn
        List<HoaDonChiTiet> danhSachChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        if (danhSachChiTiet.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errorMessage", "Không tìm thấy chi tiết hóa đơn!"));
        }

        for (HoaDonChiTiet hoaDonChiTiet : danhSachChiTiet) {
            SanPhamChiTiet spct = hoaDonChiTiet.getSanPhamChiTiet();
            if (spct != null) {
                // Cập nhật số lượng
                int soLuongMoi = spct.getSoLuong() + hoaDonChiTiet.getSoLuong();
                spct.setSoLuong(soLuongMoi);

                // Cập nhật trạng thái sản phẩm chi tiết
                if (soLuongMoi > 0) {
                    spct.setTrangThai("Còn hàng");
                } else {
                    spct.setTrangThai("Hết hàng");
                }

                sanPhamChiTietRepository.save(spct);

                // Cập nhật sản phẩm chính nếu có
                SanPham sp = spct.getSanPham();
                if (sp != null) {
                    // Cập nhật số lượng tổng
                    hoaDonChiTietService.updateStockForProduct(sp);

                    // Lấy tổng số lượng sản phẩm từ tất cả chi tiết
                    int tongSoLuong = sanPhamChiTietRepository.sumSoLuongBySanPhamId(sp.getId());

                    if (tongSoLuong > 0) {
                        sp.setTrangThai("Đang hoạt động");
                    } else {
                        sp.setTrangThai("Ngừng hoạt động");
                    }

                    sanPhamRepository.save(sp);
                }
            }
        }

        // Cập nhật lịch sử thanh toán nếu trả trước
        if ("Trả Trước".equalsIgnoreCase(hoaDon.getLoaiGiaoDich())) {
            List<LichSuThanhToan> lichSuThanhToans = lichSuThanhToanRepository.findByHoaDonId(hoaDonId);
            for (LichSuThanhToan lstt : lichSuThanhToans) {
                lstt.setTrangThai("Chưa Hoàn Tiền");
                lstt.setGhiChu("Đơn hàng bị hủy - chờ hoàn tiền");
                if (nhanVienSession != null) {
                    lstt.setNhanVien(nhanVienSession);
                }
                lichSuThanhToanRepository.save(lstt);
            }
        }

        // Ghi chú
        String ghiChuFinal = (khachHangSession != null) ? ghiChu : "Hủy bởi nhân viên";

        // Cập nhật hóa đơn
        hoaDon.setTrangThai("Đã Hủy");
        if (nhanVienSession != null) {
            hoaDon.setNhanVien(nhanVienSession);
            hoaDon.setNguoiTao(nhanVienSession.getHoTen());
            hoaDon.setNguoiSua(nhanVienSession.getHoTen());
        } else if (khachHangSession != null) {
            hoaDon.setNguoiTao(khachHangSession.getHoTen());
            hoaDon.setNguoiSua(khachHangSession.getHoTen());
        }
        hoaDonRepository.save(hoaDon);

        // Ghi lịch sử hóa đơn
        luuLichSuHoaDon(hoaDon, "Đã Hủy", ghiChuFinal, session);

        return response("Hóa đơn đã được hủy, hoàn kho và cập nhật trạng thái sản phẩm thành công.", true);
    }


    @PostMapping("/admin-huy")
    public ResponseEntity<?> huyAdmin(@RequestParam("hoaDonId") Integer hoaDonId,
                                      @RequestParam("ghiChu") String ghiChu,
                                      HttpSession session) throws Exception {
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
        KhachHang khachHangSession = (KhachHang) session.getAttribute("userKhachHang");

        if (nhanVienSession == null && khachHangSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
        }

        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);
        if (optionalHoaDon.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errorMessage", "Không tìm thấy hóa đơn!"));
        }

        HoaDon hoaDon = optionalHoaDon.get();
        String trangThaiHoaDon = hoaDon.getTrangThai();

        // Nếu KHÔNG phải trạng thái "Chờ Xác Nhận"
        if (!"Chờ Xác Nhận".equalsIgnoreCase(trangThaiHoaDon)) {
            List<HoaDonChiTiet> danhSachChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            if (danhSachChiTiet.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("errorMessage", "Không tìm thấy chi tiết hóa đơn!"));
            }

            for (HoaDonChiTiet hoaDonChiTiet : danhSachChiTiet) {
                SanPhamChiTiet spct = hoaDonChiTiet.getSanPhamChiTiet();
                if (spct != null) {
                    // Cộng lại số lượng
                    int soLuongMoi = spct.getSoLuong() + hoaDonChiTiet.getSoLuong();
                    spct.setSoLuong(soLuongMoi);

                    // Cập nhật trạng thái sản phẩm chi tiết
                    spct.setTrangThai(soLuongMoi > 0 ? "Còn Hàng" : "Hết Hàng");

                    sanPhamChiTietRepository.save(spct);

                    // Cập nhật trạng thái sản phẩm chính
                    SanPham sp = spct.getSanPham();
                    if (sp != null) {
                        hoaDonChiTietService.updateStockForProduct(sp);

                        // Tổng số lượng tất cả sản phẩm chi tiết
                        int tongSoLuong = sanPhamChiTietRepository.sumSoLuongBySanPhamId(sp.getId());

                        sp.setTrangThai(tongSoLuong > 0 ? "Đang Hoạt Động" : "Ngừng Hoạt Động");

                        sanPhamRepository.save(sp);
                    }
                }
            }

            // Nếu là phương thức trả trước (id 2 hoặc 3), cập nhật lịch sử thanh toán
            Integer ptttId = hoaDon.getPhuongThucThanhToan().getId();
            if (ptttId == 2 || ptttId == 3) {
                List<LichSuThanhToan> lichSuThanhToans = lichSuThanhToanRepository.findByHoaDonId(hoaDonId);
                for (LichSuThanhToan lstt : lichSuThanhToans) {
                    lstt.setTrangThai("Chưa Hoàn Tiền");
                    lstt.setGhiChu("Đơn hàng bị hủy - chờ hoàn tiền");
                    if (nhanVienSession != null) {
                        lstt.setNhanVien(nhanVienSession);
                    }
                    lichSuThanhToanRepository.save(lstt);
                }
            }
        }

        // Cập nhật trạng thái hóa đơn
        hoaDon.setTrangThai("Đã Hủy");

        if (nhanVienSession != null) {
            hoaDon.setNhanVien(nhanVienSession);
            hoaDon.setNguoiTao(nhanVienSession.getHoTen());
            hoaDon.setNguoiSua(nhanVienSession.getHoTen());
        } else if (khachHangSession != null) {
            hoaDon.setNguoiTao(khachHangSession.getHoTen());
            hoaDon.setNguoiSua(khachHangSession.getHoTen());
        }

        hoaDonRepository.save(hoaDon);

        luuLichSuHoaDon(hoaDon, "Đã Hủy", ghiChu, session);

        return response("Hóa đơn đã được hủy, hoàn kho và cập nhật trạng thái sản phẩm thành công.", true);
    }


}
