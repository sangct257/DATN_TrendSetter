package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.HoaDonChiTietDTO;
import com.example.datn_trendsetter.DTO.HoaDonDTO;
import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
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
    public HoaDon createHoaDon(HoaDonDTO request) {
        HoaDon hoaDon = new HoaDon();

        // ✅ Gán thông tin người nhận
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
        // ✅ Lưu tổng tiền & phí ship
        hoaDon.setTongTien(request.getTongTien());
        hoaDon.setPhiShip(request.getPhiShip());

        // ✅ Gán phương thức thanh toán
        hoaDon.setPhuongThucThanhToan(
                phuongThucThanhToanRepository.findById(request.getIdPhuongThucThanhToan()).orElse(null)
        );

        // ✅ Xử lý loại giao dịch dựa trên phương thức thanh toán
        if ("Tiền Mặt".equals(hoaDon.getPhuongThucThanhToan().getTenPhuongThuc())) {
            hoaDon.setLoaiGiaoDich("Trả Sau");
        } else {
            hoaDon.setLoaiGiaoDich("Trả Trước");
        }

        // ✅ Xử lý phiếu giảm giá nếu có
        if (request.getIdPhieuGiamGia() != null) {
            Optional<PhieuGiamGia> optionalPGG = phieuGiamGiaRepository.findById(request.getIdPhieuGiamGia());
            if (optionalPGG.isPresent()) {
                PhieuGiamGia phieuGiamGia = optionalPGG.get();
                if (phieuGiamGia.getSoLuotSuDung() > 0) {
                    phieuGiamGia.setSoLuotSuDung(phieuGiamGia.getSoLuotSuDung() - 1);
                    phieuGiamGiaRepository.save(phieuGiamGia);
                    hoaDon.setPhieuGiamGia(phieuGiamGia);
                } else {
                    throw new IllegalArgumentException("Phiếu giảm giá đã hết lượt sử dụng!");
                }
            }
        }

        // ✅ Tạo mã hóa đơn duy nhất
        hoaDon.setMaHoaDon(generateUniqueMaHoaDon());

        // ✅ Lưu hóa đơn vào DB
        hoaDon = hoaDonRepository.save(hoaDon);

        // ✅ Lưu chi tiết hóa đơn
        for (HoaDonChiTietDTO chiTietDTO : request.getHoaDonChiTiet()) {
            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHoaDon(hoaDon);
            chiTiet.setSanPhamChiTiet(
                    sanPhamChiTietRepository.findById(chiTietDTO.getIdSanPhamChiTiet()).orElse(null)
            );
            chiTiet.setSoLuong(chiTietDTO.getSoLuong());
            chiTiet.setGia(chiTietDTO.getGia());
            chiTiet.setThanhTien(chiTietDTO.getGia() * chiTietDTO.getSoLuong());
            hoaDonChiTietRepository.save(chiTiet);
        }

        // ✅ Lưu lịch sử hóa đơn
        saveLichSuHoaDon(hoaDon);

        // ✅ Lưu lịch sử thanh toán
        if (!"Tiền Mặt".equals(hoaDon.getPhuongThucThanhToan().getTenPhuongThuc())) {
            saveLichSuThanhToan(hoaDon, hoaDon.getTongTien());
        } else {
            saveLichSuThanhToan(hoaDon, 0.0f);
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
        lichSu.setGhiChu("Khách hàng đã đặt đơn hàng.");
        lichSuHoaDonRepository.save(lichSu);
        logger.info("Lịch sử hóa đơn đã lưu cho hóa đơn ID: {}", hoaDon.getId());
    }

    private void saveLichSuThanhToan(HoaDon hoaDon, Float soTien) {
        NhanVien nhanVien = hoaDon.getNhanVien();

        LichSuThanhToan lichSuThanhToan = new LichSuThanhToan();
        lichSuThanhToan.setHoaDon(hoaDon);
        lichSuThanhToan.setNhanVien(nhanVien);
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
