package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class LichSuHoaDonService {
    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private LichSuThanhToanRepository lichSuThanhToanRepository;

    public List<LichSuHoaDon> getAll() {
        return lichSuHoaDonRepository.findAll();
    }

    public void getOrderDetails(Integer hoaDonId, Model model) {
        // Kiểm tra nếu hoaDonId không phải null
        if (hoaDonId != null) {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);

            // Lấy danh sách chi tiết hóa đơn
            List<HoaDonChiTiet> hoaDonChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);


            // Lấy danh sách tất cả sản phẩm chi tiết có trạng thái "Còn Hàng"
            List<SanPhamChiTiet> allSanPhamChiTiet = sanPhamChiTietRepository.findByTrangThai("Còn Hàng");

// Tập hợp chứa các đường dẫn hình ảnh đã xuất hiện
            Set<String> seenImages = new HashSet<>();

// Danh sách sản phẩm không trùng hình ảnh
            List<SanPhamChiTiet> uniqueSanPhamChiTiet = new ArrayList<>();

            for (SanPhamChiTiet sp : allSanPhamChiTiet) {
                // Kiểm tra thêm trạng thái của sản phẩm chính: chỉ lấy khi sản phẩm chính đang hoạt động
                if (sp.getSanPham() != null && "Đang Hoạt Động".equals(sp.getSanPham().getTrangThai())) {
                    if (!sp.getHinhAnh().isEmpty()) {  // Kiểm tra nếu sản phẩm có hình ảnh
                        String imageUrl = sp.getHinhAnh().get(0).getUrlHinhAnh(); // Lấy hình ảnh đầu tiên
                        if (!seenImages.contains(imageUrl)) {
                            seenImages.add(imageUrl);
                            uniqueSanPhamChiTiet.add(sp);
                        }
                    }
                }
            }

            // Trộn danh sách để có thứ tự ngẫu nhiên
            Collections.shuffle(uniqueSanPhamChiTiet);

            // Gán vào model
            model.addAttribute("sanPhamChiTiet", uniqueSanPhamChiTiet);

            // Tính tổng tiền đã thanh toán
            float soTienDaThanhToan = lichSuThanhToanRepository
                    .sumSoTienThanhToanByHoaDonId(hoaDon.getId())
                    .orElse(0F);

            hoaDon.setSoTienDaThanhToan(soTienDaThanhToan);

            Page<KhachHang> khachHangs = khachHangRepository.findAllByTrangThai("Đang Hoạt Động", Pageable.ofSize(5));
            List<PhuongThucThanhToan> listPhuongThucThanhToan = phuongThucThanhToanRepository.findAll();

            model.addAttribute("khachHangs", khachHangs);
            model.addAttribute("listPhuongThucThanhToan", listPhuongThucThanhToan);

            // Lọc danh sách phiếu giảm giá dựa trên tổng tiền
            Float tongTien = hoaDon.getTongTien();
            if (tongTien != null) {
                List<PhieuGiamGia> validPhieuGiamGia = phieuGiamGiaRepository.findAll()
                        .stream()
                        .filter(phieu -> tongTien >= phieu.getDieuKien())
                        .toList();
                model.addAttribute("listPhieuGiamGia", validPhieuGiamGia); // Gắn danh sách phù hợp vào model
            } else {
                model.addAttribute("listPhieuGiamGia", new ArrayList<>()); // Không có phiếu giảm giá nào
            }
            List<LichSuHoaDon> listLichSuHoaDon = lichSuHoaDonRepository.findByHoaDonId(hoaDonId);
            List<LichSuThanhToan> listLichSuThanhToan = lichSuThanhToanRepository.findByHoaDonId(hoaDonId);
            // Lọc danh sách phiếu giảm giá dựa trên tổng tiền
            model.addAttribute("hoaDon", hoaDon);
            model.addAttribute("listLichSuThanhToan",listLichSuThanhToan);
            model.addAttribute("listLichSuHoaDon", listLichSuHoaDon);
            model.addAttribute("danhSachHoaDonChiTiet", hoaDonChiTiet);
            model.addAttribute("hoaDon", hoaDon);
        }
    }


    public void getHoaDon(String maHoaDon, Model model) {
        // Kiểm tra nếu hoaDonId không phải null
        if (maHoaDon != null) {
            HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(maHoaDon);

            // Lấy danh sách chi tiết hóa đơn
            List<HoaDonChiTiet> hoaDonChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());


            // Lấy danh sách tất cả sản phẩm chi tiết có trạng thái "Còn Hàng"
            List<SanPhamChiTiet> allSanPhamChiTiet = sanPhamChiTietRepository.findByTrangThai("Còn Hàng");

// Tập hợp chứa các đường dẫn hình ảnh đã xuất hiện
            Set<String> seenImages = new HashSet<>();

// Danh sách sản phẩm không trùng hình ảnh
            List<SanPhamChiTiet> uniqueSanPhamChiTiet = new ArrayList<>();

            for (SanPhamChiTiet sp : allSanPhamChiTiet) {
                // Kiểm tra thêm trạng thái của sản phẩm chính: chỉ lấy khi sản phẩm chính đang hoạt động
                if (sp.getSanPham() != null && "Đang Hoạt Động".equals(sp.getSanPham().getTrangThai())) {
                    if (!sp.getHinhAnh().isEmpty()) {  // Kiểm tra nếu sản phẩm có hình ảnh
                        String imageUrl = sp.getHinhAnh().get(0).getUrlHinhAnh(); // Lấy hình ảnh đầu tiên
                        if (!seenImages.contains(imageUrl)) {
                            seenImages.add(imageUrl);
                            uniqueSanPhamChiTiet.add(sp);
                        }
                    }
                }
            }

            // Trộn danh sách để có thứ tự ngẫu nhiên
            Collections.shuffle(uniqueSanPhamChiTiet);

            // Gán vào model
            model.addAttribute("sanPhamChiTiet", uniqueSanPhamChiTiet);

            // Tính tổng tiền đã thanh toán
            float soTienDaThanhToan = lichSuThanhToanRepository
                    .sumSoTienThanhToanByHoaDonId(hoaDon.getId())
                    .orElse(0F);

            hoaDon.setSoTienDaThanhToan(soTienDaThanhToan);

            Page<KhachHang> khachHangs = khachHangRepository.findAllByTrangThai("Đang Hoạt Động", Pageable.ofSize(5));
            List<PhuongThucThanhToan> listPhuongThucThanhToan = phuongThucThanhToanRepository.findAll();

            model.addAttribute("khachHangs", khachHangs);
            model.addAttribute("listPhuongThucThanhToan", listPhuongThucThanhToan);

            // Lọc danh sách phiếu giảm giá dựa trên tổng tiền
            Float tongTien = hoaDon.getTongTien();
            if (tongTien != null) {
                List<PhieuGiamGia> validPhieuGiamGia = phieuGiamGiaRepository.findAll()
                        .stream()
                        .filter(phieu -> tongTien >= phieu.getDieuKien())
                        .toList();
                model.addAttribute("listPhieuGiamGia", validPhieuGiamGia); // Gắn danh sách phù hợp vào model
            } else {
                model.addAttribute("listPhieuGiamGia", new ArrayList<>()); // Không có phiếu giảm giá nào
            }
            List<LichSuHoaDon> listLichSuHoaDon = lichSuHoaDonRepository.findByHoaDonId(hoaDon.getId());
            List<LichSuThanhToan> listLichSuThanhToan = lichSuThanhToanRepository.findByHoaDonId(hoaDon.getId());
            // Lọc danh sách phiếu giảm giá dựa trên tổng tiền
            model.addAttribute("hoaDon", hoaDon);
            model.addAttribute("listLichSuThanhToan",listLichSuThanhToan);
            model.addAttribute("listLichSuHoaDon", listLichSuHoaDon);
            model.addAttribute("danhSachHoaDonChiTiet", hoaDonChiTiet);
            model.addAttribute("hoaDon", hoaDon);
        }
    }

    public Map<String, Object> xacNhanHoaDon(Integer hoaDonId) {
        Map<String, Object> response = new HashMap<>();

        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(hoaDonId);
        if (optionalHoaDon.isPresent()) {
            HoaDon hoaDon = optionalHoaDon.get();

            // Kiểm tra trạng thái hiện tại của hóa đơn (tránh xác nhận 2 lần)
            if ("Đã Xác Nhận".equals(hoaDon.getTrangThai())) {
                response.put("error", "Hóa đơn đã được xác nhận trước đó!");
                return response;
            }

            // Cập nhật trạng thái hóa đơn
            hoaDon.setTrangThai("Đã Xác Nhận");
            hoaDonRepository.save(hoaDon);

            // Thêm lịch sử hóa đơn
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hoaDon);
            lichSu.setHanhDong("Đã Xác Nhận");
            lichSu.setNgayTao(LocalDateTime.now());
            lichSu.setNguoiTao(hoaDon.getNguoiTao());
            lichSu.setGhiChu("Đã Xác Nhận Hóa Đơn: " + hoaDon.getMaHoaDon());
            lichSuHoaDonRepository.save(lichSu);

            response.put("message", "Hóa đơn đã được xác nhận thành công!");
        } else {
            response.put("error", "Hóa đơn không tồn tại!");
        }

        return response;
    }
}
