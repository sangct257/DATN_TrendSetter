package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.ProductInfoDTO;
import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HoaDonChiTietService {
    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;
    @Autowired
    private HoaDonRepository hoaDonRepository;
    @Autowired
    private SanPhamRepository sanPhamRepository;
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    public ResponseEntity<Map<String, String>> addProductDetailToHoaDon(Integer sanPhamChiTietId, Integer hoaDonId, Integer soLuong) {
        Map<String, String> response = new HashMap<>();

        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(sanPhamChiTietId).orElse(null);
        if (sanPhamChiTiet == null) {
            response.put("errorMessage", "Sản phẩm chi tiết không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        if (soLuong < 1) {
            response.put("errorMessage", "Số lượng phải lớn hơn 0!");
            return ResponseEntity.badRequest().body(response);
        }

        if (sanPhamChiTiet.getSoLuong() < soLuong) {
            response.put("errorMessage", "Số lượng tồn kho không đủ.");
            return ResponseEntity.badRequest().body(response);
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            response.put("errorMessage", "Hóa đơn không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        // Cập nhật hoặc thêm mới sản phẩm vào hóa đơn
        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findByHoaDonIdAndSanPhamChiTietId(hoaDonId, sanPhamChiTietId)
                .orElse(new HoaDonChiTiet());
        hoaDonChiTiet.setHoaDon(hoaDon);
        hoaDonChiTiet.setSanPhamChiTiet(sanPhamChiTiet);
        hoaDonChiTiet.setSoLuong(hoaDonChiTiet.getSoLuong() != null ? hoaDonChiTiet.getSoLuong() + soLuong : soLuong);
        hoaDonChiTiet.setGia(sanPhamChiTiet.getGia().floatValue());
        hoaDonChiTiet.setThanhTien(hoaDonChiTiet.getSoLuong() * hoaDonChiTiet.getGia());
        hoaDonChiTietRepository.save(hoaDonChiTiet);

        // Trừ số lượng tồn kho
        sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() - soLuong);
        sanPhamChiTiet.setTrangThai(sanPhamChiTiet.getSoLuong() == 0 ? "Hết Hàng" : "Còn Hàng");
        sanPhamChiTietRepository.save(sanPhamChiTiet);

        // Cập nhật số lượng tồn kho cho sản phẩm chính
        updateStockForProduct(sanPhamChiTiet.getSanPham());

        // Cập nhật tổng tiền hóa đơn
        updateInvoiceTotal(hoaDonId);

        // Tìm phiếu giảm giá tốt nhất (nếu có)
        PhieuGiamGia bestVoucher = findBestVoucherForInvoice(hoaDon.getTongTien());
        hoaDon.setPhieuGiamGia(bestVoucher); // Nếu không có phiếu giảm giá phù hợp, sẽ là null
        hoaDonRepository.save(hoaDon);

        // Trả về trạng thái hóa đơn
        response.put("successMessage", "Thêm sản phẩm vào hóa đơn thành công!");
        response.put("trangThai", hoaDon.getTrangThai());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String,String>> updateQuantityOrder(Integer hoaDonChiTietId, Integer soLuong, Integer hoaDonId) {
        Map<String,String> response = new HashMap<>();
        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(hoaDonChiTietId).orElse(null);
        if (hoaDonChiTiet == null) {
            response.put("errorMessage","Hóa đơn chi tiết không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        if (soLuong < 1) {
            response.put("errorMessage","Số lượng phải lớn hơn 0!");
            return ResponseEntity.badRequest().body(response);
        }

        SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();
        int soLuongTonKho = sanPhamChiTiet.getSoLuong();
        int soLuongBanDau = hoaDonChiTiet.getSoLuong();
        int thayDoiSoLuong = soLuong - soLuongBanDau;

        // Kiểm tra tồn kho hợp lệ
        if (thayDoiSoLuong > 0 && soLuongTonKho < thayDoiSoLuong) {
            response.put("errorMessage", "Số lượng tồn kho không đủ.");
            return ResponseEntity.badRequest().body(response);
        }

        // Cập nhật số lượng hóa đơn chi tiết
        hoaDonChiTiet.setSoLuong(soLuong);
        hoaDonChiTiet.setThanhTien(sanPhamChiTiet.getGia().floatValue() * soLuong);
        hoaDonChiTietRepository.save(hoaDonChiTiet);

        // Cập nhật tồn kho
        sanPhamChiTiet.setSoLuong(soLuongTonKho - thayDoiSoLuong);
        sanPhamChiTiet.setTrangThai(sanPhamChiTiet.getSoLuong() == 0 ? "Hết Hàng" : "Còn Hàng");
        sanPhamChiTietRepository.save(sanPhamChiTiet);

        // Cập nhật tồn kho sản phẩm chính
        updateStockForProduct(sanPhamChiTiet.getSanPham());

        // Cập nhật tổng tiền hóa đơn
        updateInvoiceTotal(hoaDonId);

        // Kiểm tra và cập nhật phiếu giảm giá
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);

        // Tìm phiếu giảm giá tốt nhất (nếu có)
        PhieuGiamGia bestVoucher = findBestVoucherForInvoice(hoaDon.getTongTien());
        hoaDon.setPhieuGiamGia(bestVoucher); // Nếu không có phiếu giảm giá phù hợp, sẽ là null
        hoaDonRepository.save(hoaDon);

        // Trả về trạng thái hóa đơn
        response.put("successMessage", "Sửa sản phẩm vào hóa đơn thành công!");
        response.put("trangThai", hoaDon.getTrangThai());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String,String>> deleteProductOrder(Integer hoaDonChiTietId, Integer hoaDonId) {
        Map<String,String> response = new HashMap<>();
        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(hoaDonChiTietId).orElse(null);
        if (hoaDonChiTiet == null) {
            response.put("errorMessage","Hóa đơn chi tiết không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        // Hoàn trả lại số lượng tồn kho cho sản phẩm chi tiết
        SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();
        if (sanPhamChiTiet != null) {
            sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() + hoaDonChiTiet.getSoLuong());
            sanPhamChiTietRepository.save(sanPhamChiTiet);

            // Cập nhật số lượng tồn kho cho sản phẩm chính
            SanPham sanPham = sanPhamChiTiet.getSanPham();
            updateStockForProduct(sanPham);
        }

        // Xóa sản phẩm khỏi hóa đơn
        hoaDonChiTietRepository.delete(hoaDonChiTiet);

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            response.put("errorMessage","Hóa đơn không tồn tại.");
            return ResponseEntity.badRequest().body(response);
        }
        // Cập nhật tổng tiền hóa đơn sau khi xóa sản phẩm
        updateInvoiceTotal(hoaDonId);

        // Tìm phiếu giảm giá tốt nhất (nếu có)
        PhieuGiamGia bestVoucher = findBestVoucherForInvoice(hoaDon.getTongTien());
        hoaDon.setPhieuGiamGia(bestVoucher); // Nếu không có phiếu giảm giá phù hợp, sẽ là null
        hoaDonRepository.save(hoaDon);

        // Trả về trạng thái hóa đơn
        response.put("successMessage", "Xóa sản phẩm vào hóa đơn thành công!");
        response.put("trangThai", hoaDon.getTrangThai());
        return ResponseEntity.ok(response);
    }

    // Phương thức cập nhật tồn kho cho sản phẩm chính
    private void updateStockForProduct(SanPham sanPham) {
        List<SanPhamChiTiet> listSanPhamChiTiet = sanPhamChiTietRepository.findBySanPham(sanPham);
        int tongSoLuong = listSanPhamChiTiet.stream()
                .filter(spct -> spct.getSoLuong() != null)
                .mapToInt(SanPhamChiTiet::getSoLuong)
                .sum();
        sanPham.setSoLuong(tongSoLuong);
        sanPhamRepository.save(sanPham);
    }

    // Phương thức cập nhật tổng tiền hóa đơn
    private void updateInvoiceTotal(Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon != null) {
            Float tongTien = (float) hoaDonChiTietRepository.findByHoaDonId(hoaDonId).stream()
                    .mapToDouble(HoaDonChiTiet::getThanhTien)
                    .sum();
            hoaDon.setTongTien(tongTien);
            hoaDonRepository.save(hoaDon);
        }
    }

    public PhieuGiamGia findBestVoucherForInvoice(Float tongTien) {
        // Lấy danh sách các phiếu giảm giá còn hiệu lực và đang hoạt động
        List<PhieuGiamGia> availableVouchers = phieuGiamGiaRepository.findAllByTrangThai("Đang Hoạt Động");

        // Lọc các phiếu giảm giá có điều kiện phù hợp với tổng tiền hóa đơn
        List<PhieuGiamGia> validVouchers = availableVouchers.stream()
                .filter(voucher -> tongTien >= voucher.getDieuKien())  // Điều kiện tổng tiền >= điều kiện phiếu giảm giá
                .filter(voucher -> voucher.getNgayBatDau().isBefore(LocalDateTime.now()) &&  // Phiếu giảm giá còn hiệu lực
                        voucher.getNgayKetThuc().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(PhieuGiamGia::getGiaTri).reversed())  // Sắp xếp giảm dần theo giá trị phiếu
                .toList();

        // Trả về phiếu giảm giá có giá trị cao nhất nếu có, nếu không trả về null
        return validVouchers.isEmpty() ? null : validVouchers.get(0);
    }



    public int getTongSanPhamBanTrongThang() {
        return hoaDonChiTietRepository.getTongSanPhamBanTrongThang();
    }

    public Page<Object[]> getTotalSoldByProductInMonthWithImages(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size); // page bắt đầu từ 0
        return hoaDonChiTietRepository.getTotalSoldByProductInMonth(pageable);
    }
}
