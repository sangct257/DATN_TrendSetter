package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.HoaDonChiTietDTO;
import com.example.datn_trendsetter.DTO.HoaDonDTO;
import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;

@Service
public class HoaDonOnlineService {
    private static final Logger logger = LoggerFactory.getLogger(HoaDonOnlineService.class);

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private LichSuThanhToanRepository lichSuThanhToanRepository;

    @Transactional
    public HoaDon createHoaDon(HoaDonDTO request, HttpSession session) {
        HoaDon hoaDon = new HoaDon();

        // Kiểm tra khách hàng đã đăng nhập hay chưa
        KhachHang khachHang = (KhachHang) session.getAttribute("userKhachHang");
        if (khachHang != null) {
            hoaDon.setKhachHang(khachHang); // Gán khách hàng vào hóa đơn
        }

        // Gán thông tin người nhận
        hoaDon.setNguoiNhan(request.getNguoiNhan());
        hoaDon.setSoDienThoai(request.getSoDienThoai());
        hoaDon.setEmail(request.getEmail());
        hoaDon.setDiaChiCuThe(request.getDiaChiCuThe());
        hoaDon.setThanhPho(request.getThanhPho());
        hoaDon.setHuyen(request.getHuyen());
        hoaDon.setPhuong(request.getPhuong());
        hoaDon.setGhiChu(request.getGhiChu());
        hoaDon.setTrangThai("Chờ Xác Nhận");
        hoaDon.setLoaiHoaDon("Online");
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setThoiGianNhanDuKien(LocalDate.now().plusDays(3));

        // Lưu tổng tiền & phí ship
        hoaDon.setTongTien(request.getTongTien());
        hoaDon.setPhiShip(request.getPhiShip());

        // Kiểm tra tổng tiền
        if (hoaDon.getTongTien().compareTo(Float.valueOf(50000000)) > 0) {
            throw new IllegalStateException("Đơn hàng vượt quá 50.000.000đ. Vui lòng liên hệ trực tiếp với cửa hàng để được hỗ trợ.");
        }

        // Gán phương thức thanh toán
        hoaDon.setPhuongThucThanhToan(
                phuongThucThanhToanRepository.findById(request.getIdPhuongThucThanhToan()).orElse(null)
        );

        // Xử lý phiếu giảm giá nếu có
        if (request.getIdPhieuGiamGia() != null) {
            Optional<PhieuGiamGia> optionalPGG = phieuGiamGiaRepository.findById(request.getIdPhieuGiamGia());
            if (optionalPGG.isPresent()) {
                PhieuGiamGia phieuGiamGia = optionalPGG.get();

                // Kiểm tra trạng thái phiếu trước khi áp dụng
                if (!"Đang Hoạt Động".equalsIgnoreCase(phieuGiamGia.getTrangThai())) {
                    throw new IllegalStateException("Phiếu giảm giá đã hết hạn.");
                }

                hoaDon.setPhieuGiamGia(phieuGiamGia);

                // Nếu phương thức thanh toán là "Tiền Mặt" (ID = 1), thì trừ lượt luôn
                if (request.getIdPhuongThucThanhToan() == 1) {
                    if (phieuGiamGia.getSoLuotSuDung() > 0) {
                        phieuGiamGia.setSoLuotSuDung(phieuGiamGia.getSoLuotSuDung() - 1);
                        phieuGiamGia.setTrangThai(phieuGiamGia.getSoLuotSuDung() > 0 ? "Đang Hoạt Động" : "Ngừng Hoạt Động");
                        phieuGiamGiaRepository.save(phieuGiamGia);
                    } else {
                        throw new IllegalStateException("Phiếu giảm giá đã hết lượt sử dụng.");
                    }
                }
            }
        }

        // Xử lý loại giao dịch dựa trên phương thức thanh toán
        if ("Tiền Mặt".equals(hoaDon.getPhuongThucThanhToan().getTenPhuongThuc())) {
            hoaDon.setLoaiGiaoDich("Trả Sau");
        } else {
            hoaDon.setLoaiGiaoDich("Trả Trước");
        }

        // Tạo mã hóa đơn duy nhất
        hoaDon.setMaHoaDon(generateUniqueMaHoaDon());

        // Lưu hóa đơn vào DB
        hoaDon = hoaDonRepository.save(hoaDon);

        // Lưu chi tiết hóa đơn
        for (HoaDonChiTietDTO chiTietDTO : request.getHoaDonChiTiet()) {
            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHoaDon(hoaDon);

            // Lấy sản phẩm chi tiết từ ID
            Optional<SanPhamChiTiet> sanPhamChiTiet = sanPhamChiTietRepository.findById(chiTietDTO.getIdSanPhamChiTiet());
            if (sanPhamChiTiet.isPresent()) {
                SanPhamChiTiet chiTietSanPham = sanPhamChiTiet.get();

                // Kiểm tra trạng thái sản phẩm
                if (!"Đang Hoạt Động".equalsIgnoreCase(chiTietSanPham.getSanPham().getTrangThai())) {
                    throw new IllegalStateException("Sản phẩm đã ngừng bán.");
                }

                // Kiểm tra giá hiện tại
                if (!chiTietSanPham.getGia().equals(chiTietDTO.getGia())) {
                    throw new IllegalStateException("Giá sản phẩm đã thay đổi. Vui lòng kiểm tra lại giỏ hàng.");
                }

                // Thiết lập chi tiết sản phẩm vào hóa đơn
                chiTiet.setSanPhamChiTiet(chiTietSanPham);
                chiTiet.setSoLuong(chiTietDTO.getSoLuong());
                chiTiet.setGia(chiTietDTO.getGia());
                chiTiet.setThanhTien(chiTietDTO.getGia() * chiTietDTO.getSoLuong());
                hoaDonChiTietRepository.save(chiTiet);
            } else {
                throw new IllegalStateException("Sản phẩm chi tiết không hợp lệ.");
            }

        }

        // Lưu lịch sử hóa đơn
        saveLichSuHoaDon(hoaDon);

        // Lưu lịch sử thanh toán
        if (!"Tiền Mặt".equals(hoaDon.getPhuongThucThanhToan().getTenPhuongThuc())) {
            saveLichSuThanhToan(hoaDon, hoaDon.getTongTien());
        }

        // Nếu phương thức thanh toán là VNPAY, set trạng thái là "Chưa Thanh Toán"
        if ("VNPAY".equals(hoaDon.getPhuongThucThanhToan().getTenPhuongThuc())) {
            hoaDon.setTrangThai("Chưa Thanh Toán");
        }

        return hoaDon;
    }

    // ✅ Tạo mã hóa đơn duy nhất
    private String generateUniqueMaHoaDon() {
        String maHoaDon;
        Random random = new Random();
        do {
            int randomNumber = 100000 + random.nextInt(900000);
            maHoaDon = "HD" + randomNumber;
        } while (hoaDonRepository.existsByMaHoaDon(maHoaDon));
        return maHoaDon;
    }

    // ✅ Lưu lịch sử hóa đơn
    private void saveLichSuHoaDon(HoaDon hoaDon) {
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setHanhDong(hoaDon.getTrangThai());
        lichSu.setNgayTao(LocalDateTime.now());
        lichSu.setNguoiTao(hoaDon.getNguoiNhan());
        lichSu.setNguoiSua(hoaDon.getNguoiNhan());
        lichSu.setKhachHang(hoaDon.getKhachHang());
        lichSu.setDeleted(false);
        lichSu.setGhiChu("Khách hàng đã đặt đơn hàng.");
        lichSuHoaDonRepository.save(lichSu);
        logger.info("Lịch sử hóa đơn đã lưu cho hóa đơn ID: {}", hoaDon.getId());
    }


    private void saveLichSuThanhToan(HoaDon hoaDon, Float soTien) {
        NhanVien nhanVien = hoaDon.getNhanVien();

        LichSuThanhToan lichSuThanhToan = new LichSuThanhToan();
        lichSuThanhToan.setHoaDon(hoaDon);
        lichSuThanhToan.setNhanVien(nhanVien);
        lichSuThanhToan.setNguoiXacNhan(hoaDon.getKhachHang().getHoTen());
        lichSuThanhToan.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        lichSuThanhToan.setSoTienThanhToan(soTien);
        lichSuThanhToan.setThoiGianThanhToan(LocalDateTime.now());
        lichSuThanhToan.setTrangThai("Đã Thanh Toán");
        lichSuThanhToan.setGhiChu("Thanh toán hóa đơn " + hoaDon.getMaHoaDon());

        if (!"Tiền Mặt".equals(hoaDon.getPhuongThucThanhToan().getTenPhuongThuc())) {
            lichSuThanhToan.setMaGiaoDich(generateTransactionCode());
        }

        lichSuThanhToanRepository.save(lichSuThanhToan);
    }

    private String generateTransactionCode() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timePart = LocalDateTime.now().format(formatter);
        int randomPart = new Random().nextInt(900000) + 100000; // Số ngẫu nhiên 6 chữ số
        return "GD" + timePart + randomPart; // VD: GD20240321123045123456
    }

}
