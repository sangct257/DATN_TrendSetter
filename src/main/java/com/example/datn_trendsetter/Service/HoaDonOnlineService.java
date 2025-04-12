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

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Transactional
    public HoaDon createHoaDon(HoaDonDTO request) {
        logger.info("=== Bắt đầu tạo hóa đơn mới ===");
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
        hoaDon.setLoaiGiaoDich("Trả Sau");
        hoaDon.setNgayTao(LocalDateTime.now());

        // ✅ Log giá trị trước khi lưu vào DB
        logger.info("Tổng tiền từ request: {}", request.getTongTien());
        logger.info("Phí ship từ request: {}", request.getPhiShip());

        // ✅ Lưu tổng tiền & phí ship
        hoaDon.setTongTien(request.getTongTien());
        hoaDon.setPhiShip(request.getPhiShip());

        // ✅ Gán phương thức thanh toán
        hoaDon.setPhuongThucThanhToan(
                phuongThucThanhToanRepository.findById(request.getIdPhuongThucThanhToan()).orElse(null)
        );

        // ✅ Xử lý phiếu giảm giá nếu có
        if (request.getIdPhieuGiamGia() != null) {
            Optional<PhieuGiamGia> optionalPGG = phieuGiamGiaRepository.findById(request.getIdPhieuGiamGia());
            if (optionalPGG.isPresent()) {
                PhieuGiamGia phieuGiamGia = optionalPGG.get();
                if (phieuGiamGia.getSoLuotSuDung() > 0) {
                    phieuGiamGia.setSoLuotSuDung(phieuGiamGia.getSoLuotSuDung() - 1); // Trừ số lượt sử dụng
                    phieuGiamGiaRepository.save(phieuGiamGia);
                    hoaDon.setPhieuGiamGia(phieuGiamGia);
                } else {
                    throw new IllegalArgumentException("Phiếu giảm giá đã hết lượt sử dụng!");
                }
            }
        }

        // ✅ Tạo mã hóa đơn duy nhất
        hoaDon.setMaHoaDon(generateUniqueMaHoaDon());
        hoaDon.setThoiGianNhanDuKien(LocalDate.now().plusDays(3));
        // ✅ Lưu hóa đơn vào DB
        hoaDon = hoaDonRepository.save(hoaDon);

        // ✅ Log sau khi lưu vào DB
        logger.info("Hóa đơn đã lưu với ID: {}", hoaDon.getId());
        logger.info("Tổng tiền sau khi lưu DB: {}", hoaDon.getTongTien());
        logger.info("Phí ship sau khi lưu DB: {}", hoaDon.getPhiShip());

        // ✅ Lưu chi tiết hóa đơn
        for (HoaDonChiTietDTO chiTietDTO : request.getHoaDonChiTiet()) {
            SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository
                    .findById(chiTietDTO.getIdSanPhamChiTiet())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm chi tiết không tồn tại!"));

            // Kiểm tra số lượng tồn kho
            if (sanPhamChiTiet.getSoLuong() < chiTietDTO.getSoLuong()) {
                throw new IllegalArgumentException("Số lượng sản phẩm không đủ!");
            }

            // Giảm số lượng tồn kho của sản phẩm chi tiết
            sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() - chiTietDTO.getSoLuong());
            sanPhamChiTiet.setTrangThai(sanPhamChiTiet.getSoLuong() == 0 ? "Hết Hàng" : "Còn Hàng");
            sanPhamChiTietRepository.save(sanPhamChiTiet);

            // Tạo chi tiết hóa đơn
            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHoaDon(hoaDon);
            chiTiet.setSanPhamChiTiet(sanPhamChiTiet);
            chiTiet.setSoLuong(chiTietDTO.getSoLuong());
            chiTiet.setGia(chiTietDTO.getGia());
            chiTiet.setThanhTien(chiTietDTO.getGia() * chiTietDTO.getSoLuong());
            hoaDonChiTietRepository.save(chiTiet);

            // Cập nhật số lượng tổng của sản phẩm chính
            updateSoLuongSanPham(sanPhamChiTiet.getSanPham());

            // Log chi tiết
            logger.info("Cập nhật sản phẩm ID: {}, Số lượng còn lại: {}",
                    sanPhamChiTiet.getId(), sanPhamChiTiet.getSoLuong());
        }

        // ✅ Lưu lịch sử hóa đơn
        saveLichSuHoaDon(hoaDon);

        saveLichSuThanhToan(hoaDon,0.0f);
        logger.info("=== Hoàn thành tạo hóa đơn ===");
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

    private void updateSoLuongSanPham(SanPham sanPham) {
        int tongSoLuong = sanPhamChiTietRepository.tinhTongSoLuongTheoSanPham(sanPham.getId());
        sanPham.setSoLuong(tongSoLuong);

        // ✅ Cập nhật trạng thái của sản phẩm chính
        sanPham.setTrangThai(tongSoLuong == 0 ? "Không Hoạt Động" : "Đang Hoạt Động");

        sanPhamRepository.save(sanPham);
    }



}
