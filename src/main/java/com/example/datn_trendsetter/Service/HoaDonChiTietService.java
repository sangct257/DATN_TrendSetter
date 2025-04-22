package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
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
    private LichSuThanhToanRepository lichSuThanhToanRepository;

    @Transactional
    public ResponseEntity<Map<String, String>> addProductDetailToHoaDon(Integer sanPhamChiTietId, Integer hoaDonId, Integer soLuong) {
        Map<String, String> response = new HashMap<>();

        // Kiểm tra sản phẩm chi tiết có tồn tại không
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(sanPhamChiTietId).orElse(null);
        if (sanPhamChiTiet == null) {
            response.put("errorMessage", "Sản phẩm chi tiết không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra số lượng nhập vào
        if (soLuong < 1) {
            response.put("errorMessage", "Số lượng phải lớn hơn 0!");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra số lượng tồn kho
        if (sanPhamChiTiet.getSoLuong() < soLuong) {
            response.put("errorMessage", "Số lượng tồn kho không đủ.");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra hóa đơn tồn tại
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            response.put("errorMessage", "Hóa đơn không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra xem sản phẩm đã có trong hóa đơn chưa
        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findByHoaDonIdAndSanPhamChiTietId(hoaDonId, sanPhamChiTietId)
                .orElse(new HoaDonChiTiet());

        int soLuongHienTai = hoaDonChiTiet.getId() != null ? hoaDonChiTiet.getSoLuong() : 0;
        int soLuongMoi = soLuongHienTai + soLuong;
        hoaDonChiTiet.setHoaDon(hoaDon);
        hoaDonChiTiet.setSanPhamChiTiet(sanPhamChiTiet);
        hoaDonChiTiet.setSoLuong(soLuongMoi);
        hoaDonChiTiet.setGia(sanPhamChiTiet.getGia().floatValue());
        hoaDonChiTiet.setThanhTien(soLuongMoi * hoaDonChiTiet.getGia());

        hoaDonChiTietRepository.save(hoaDonChiTiet);

        // Giảm số lượng tồn kho của sản phẩm chi tiết
        sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() - soLuong);

        // Cập nhật trạng thái sản phẩm
        sanPhamChiTiet.setTrangThai(sanPhamChiTiet.getSoLuong() == 0 ? "Hết Hàng" : "Còn Hàng");
        sanPhamChiTietRepository.save(sanPhamChiTiet);

        // Cập nhật số lượng tồn kho của sản phẩm chính
        updateStockForProduct(sanPhamChiTiet.getSanPham());

        // Tính tổng tiền hóa đơn
        float tongTienSanPham = (float) hoaDonChiTietRepository.findByHoaDonId(hoaDonId).stream()
                .mapToDouble(HoaDonChiTiet::getThanhTien)
                .sum();

        float phiShip = (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : 0;
        PhieuGiamGia phieuGiamGia = hoaDon.getPhieuGiamGia();
        float giaTriGiam = (phieuGiamGia != null && phieuGiamGia.getGiaTriGiam() != null) ? phieuGiamGia.getGiaTriGiam() : 0;

        float tongTienFinal = Math.max(0, tongTienSanPham + phiShip - giaTriGiam);
        hoaDon.setTongTien(tongTienFinal);
        hoaDonRepository.save(hoaDon);

        // Trả về thông tin cập nhật
        response.put("successMessage", "Thêm sản phẩm vào hóa đơn thành công!");
        response.put("tongTien", String.valueOf(hoaDon.getTongTien()));
        response.put("trangThai", hoaDon.getTrangThai());
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateQuantityOrder(Integer hoaDonChiTietId, Integer soLuong, Integer hoaDonId) {
        Map<String, String> response = new HashMap<>();

        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(hoaDonChiTietId).orElse(null);
        if (hoaDonChiTiet == null) {
            response.put("errorMessage", "Hóa đơn chi tiết không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        if (soLuong < 1) {
            response.put("errorMessage", "Số lượng phải lớn hơn 0!");
            return ResponseEntity.badRequest().body(response);
        }

        SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();
        if (sanPhamChiTiet == null) {
            response.put("errorMessage", "Sản phẩm chi tiết không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        int soLuongTonKho = sanPhamChiTiet.getSoLuong();
        int soLuongCu = hoaDonChiTiet.getSoLuong();
        int chenhLechSoLuong = soLuong - soLuongCu;

        if (chenhLechSoLuong > 0 && soLuongTonKho < chenhLechSoLuong) {
            response.put("errorMessage", "Số lượng tồn kho không đủ.");
            return ResponseEntity.badRequest().body(response);
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            response.put("errorMessage", "Hóa đơn không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        // ✅ Tính tổng số tiền đã thanh toán
        double tongSoTienDaThanhToan = lichSuThanhToanRepository.findByHoaDonId(hoaDonId)
                .stream()
                .mapToDouble(LichSuThanhToan::getSoTienThanhToan)
                .sum();

        // ✅ Tính tổng tiền hóa đơn nếu cập nhật số lượng
        float tongTienSanPhamSauCapNhat = (float) hoaDonChiTietRepository.findByHoaDonId(hoaDonId).stream()
                .filter(ct -> !ct.getId().equals(hoaDonChiTietId))
                .mapToDouble(HoaDonChiTiet::getThanhTien)
                .sum()
                + (sanPhamChiTiet.getGia().floatValue() * soLuong);

        float phiShip = hoaDon.getPhiShip() != null ? hoaDon.getPhiShip() : 0;
        float tongTienSauCapNhat = tongTienSanPhamSauCapNhat + phiShip;

        // ✅ Không cho phép giảm số lượng nếu tổng tiền đã thanh toán vượt quá tổng tiền mới
        if (tongSoTienDaThanhToan > tongTienSauCapNhat) {
            response.put("errorMessage", "Không thể giảm số lượng vì tổng tiền đã thanh toán vượt quá tổng tiền mới!");
            return ResponseEntity.badRequest().body(response);
        }

        // ✅ Cập nhật số lượng hóa đơn chi tiết
        hoaDonChiTiet.setSoLuong(soLuong);
        hoaDonChiTiet.setThanhTien(sanPhamChiTiet.getGia().floatValue() * soLuong);
        hoaDonChiTietRepository.save(hoaDonChiTiet);

        // ✅ Cập nhật tồn kho sản phẩm
        sanPhamChiTiet.setSoLuong(soLuongTonKho - chenhLechSoLuong);
        sanPhamChiTiet.setTrangThai(sanPhamChiTiet.getSoLuong() == 0 ? "Hết Hàng" : "Còn Hàng");
        sanPhamChiTietRepository.save(sanPhamChiTiet);

        // ✅ Cập nhật tổng tiền hóa đơn
        PhieuGiamGia phieuGiamGia = hoaDon.getPhieuGiamGia();
        float giaTriGiam = (phieuGiamGia != null) ? phieuGiamGia.getGiaTriGiam() : 0;
        float tongTienFinal = Math.max(0, tongTienSauCapNhat - giaTriGiam);
        hoaDon.setTongTien(tongTienFinal);
        hoaDonRepository.save(hoaDon);

        response.put("successMessage", "Cập nhật số lượng sản phẩm thành công!");
        response.put("trangThai", hoaDon.getTrangThai());
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<Map<String, String>> deleteProductOrder(Integer hoaDonChiTietId, Integer hoaDonId) {
        Map<String, String> response = new HashMap<>();

        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(hoaDonChiTietId).orElse(null);
        if (hoaDonChiTiet == null) {
            response.put("errorMessage", "Hóa đơn chi tiết không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            response.put("errorMessage", "Hóa đơn không tồn tại.");
            return ResponseEntity.badRequest().body(response);
        }

        // Tính tổng tiền đã thanh toán
        Float tongTienDaThanhToan = lichSuThanhToanRepository.findByHoaDonId(hoaDonId)
                .stream().map(LichSuThanhToan::getSoTienThanhToan)
                .reduce(0f, Float::sum);

        // Nếu tổng tiền đã thanh toán >= tổng tiền hóa đơn thì không cho xóa
        if (tongTienDaThanhToan >= hoaDon.getTongTien()) {
            response.put("errorMessage", "Hóa đơn đã thanh toán đầy đủ, không thể xóa sản phẩm!");
            return ResponseEntity.badRequest().body(response);
        }

        // Lấy thông tin phiếu giảm giá
        PhieuGiamGia phieuGiamGia = hoaDon.getPhieuGiamGia();
        float dieuKienGiamGia = (phieuGiamGia != null) ? phieuGiamGia.getDieuKien() : 0;

        // Tính tổng tiền sản phẩm trước khi xóa
        float tongTienSanPham = hoaDon.getHoaDonChiTiet().stream()
                .map(HoaDonChiTiet::getThanhTien)
                .reduce(0f, Float::sum);

        // Tổng tiền sản phẩm sau khi xóa
        float tongTienSanPhamSauXoa = tongTienSanPham - hoaDonChiTiet.getThanhTien();

        // Kiểm tra nếu tổng tiền sau khi xóa không đủ điều kiện áp dụng phiếu giảm giá => Không cho phép xóa
        if (phieuGiamGia != null && tongTienSanPhamSauXoa < dieuKienGiamGia) {
            response.put("errorMessage", "Không thể xóa sản phẩm vì tiền hàng không đủ điều kiện áp dụng phiếu giảm giá.");
            return ResponseEntity.badRequest().body(response);
        }

        // Hoàn trả số lượng sản phẩm
        SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();
        if (sanPhamChiTiet != null) {
            sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() + hoaDonChiTiet.getSoLuong());
            sanPhamChiTiet.setTrangThai(sanPhamChiTiet.getSoLuong() > 0 ? "Còn Hàng" : "Hết Hàng");
            sanPhamChiTietRepository.save(sanPhamChiTiet);
            updateStockForProduct(sanPhamChiTiet.getSanPham());
        }

        // Xóa sản phẩm khỏi hóa đơn
        hoaDon.getHoaDonChiTiet().removeIf(chiTiet -> chiTiet.getId().equals(hoaDonChiTietId));
        hoaDonChiTietRepository.delete(hoaDonChiTiet);

        // Cập nhật tổng tiền hóa đơn (tổng tiền sau khi xóa + phí ship - giảm giá)
        float phiShip = (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : 0;
        float giaTriGiam = (phieuGiamGia != null) ? phieuGiamGia.getGiaTriGiam() : 0;
        float tongTienSauXoa = Math.max(0, tongTienSanPhamSauXoa + phiShip - giaTriGiam);

        hoaDon.setTongTien(tongTienSauXoa);
        hoaDonRepository.save(hoaDon);

        response.put("successMessage", "Xóa sản phẩm khỏi hóa đơn thành công!");
        response.put("trangThai", hoaDon.getTrangThai());
        return ResponseEntity.ok(response);
    }

    // Phương thức cập nhật tồn kho cho sản phẩm chính
    public void updateStockForProduct(SanPham sanPham) {
        List<SanPhamChiTiet> listSanPhamChiTiet = sanPhamChiTietRepository.findBySanPham(sanPham);
        int tongSoLuong = listSanPhamChiTiet.stream()
                .filter(spct -> spct.getSoLuong() != null)
                .mapToInt(SanPhamChiTiet::getSoLuong)
                .sum();
        sanPham.setSoLuong(tongSoLuong);
        sanPhamRepository.save(sanPham);
    }

    public int getTongSanPhamBanTrongThang() {
        return hoaDonChiTietRepository.getTongSanPhamBanTrongThang("Đã Hoàn Thành");
    }
    public List<Object[]> getTotalSoldByProductInMonthWithImages() {
        return hoaDonChiTietRepository.getTotalSoldByProductInMonth("Đã Hoàn Thành"); // Trả về danh sách sản phẩm
    }

}