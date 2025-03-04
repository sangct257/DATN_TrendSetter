package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.ProductInfoDTO;
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

        // Cập nhật hoặc thêm mới sản phẩm vào hóa đơn chi tiết
        int soLuongMoi = (hoaDonChiTiet.getSoLuong() != null ? hoaDonChiTiet.getSoLuong() : 0) + soLuong;
        hoaDonChiTiet.setHoaDon(hoaDon);
        hoaDonChiTiet.setSanPhamChiTiet(sanPhamChiTiet);
        hoaDonChiTiet.setSoLuong(soLuongMoi);
        hoaDonChiTiet.setGia(sanPhamChiTiet.getGia().floatValue());
        hoaDonChiTiet.setThanhTien(soLuongMoi * hoaDonChiTiet.getGia());

        hoaDonChiTietRepository.save(hoaDonChiTiet);

        // Giảm số lượng tồn kho của sản phẩm chi tiết
        sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() - soLuong);

        // Chỉ cập nhật trạng thái nếu sản phẩm đang là "Còn Hàng"
        if (sanPhamChiTiet.getSoLuong() == 0 && "Còn Hàng".equals(sanPhamChiTiet.getTrangThai())) {
            sanPhamChiTiet.setTrangThai("Hết Hàng");
        }

        sanPhamChiTietRepository.save(sanPhamChiTiet);

        // Cập nhật số lượng tồn kho của sản phẩm chính
        updateStockForProduct(sanPhamChiTiet.getSanPham());

        // Cập nhật tổng tiền hóa đơn
        updateInvoiceTotal(hoaDonId);

        // 🔥 LẤY LẠI hóa đơn từ DB để đảm bảo tổng tiền mới nhất
        hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);

        // Trả về thông tin cập nhật
        response.put("successMessage", "Thêm sản phẩm vào hóa đơn thành công!");
        response.put("tongTien", String.valueOf(hoaDon.getTongTien()));
        response.put("trangThai", hoaDon.getTrangThai());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> updateQuantityOrder(Integer hoaDonChiTietId, Integer soLuong, Integer hoaDonId) {
        Map<String, String> response = new HashMap<>();

        // Kiểm tra hóa đơn chi tiết có tồn tại không
        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(hoaDonChiTietId).orElse(null);
        if (hoaDonChiTiet == null) {
            response.put("errorMessage", "Hóa đơn chi tiết không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra số lượng hợp lệ
        if (soLuong < 1) {
            response.put("errorMessage", "Số lượng phải lớn hơn 0!");
            return ResponseEntity.badRequest().body(response);
        }

        // Lấy sản phẩm chi tiết và kiểm tra tồn kho
        SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();
        if (sanPhamChiTiet == null) {
            response.put("errorMessage", "Sản phẩm chi tiết không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        int soLuongTonKho = sanPhamChiTiet.getSoLuong();
        int soLuongCu = hoaDonChiTiet.getSoLuong();
        int chenhLechSoLuong = soLuong - soLuongCu; // Số lượng thay đổi (có thể âm nếu giảm)

        // Nếu tăng số lượng, kiểm tra tồn kho
        if (chenhLechSoLuong > 0 && soLuongTonKho < chenhLechSoLuong) {
            response.put("errorMessage", "Số lượng tồn kho không đủ.");
            return ResponseEntity.badRequest().body(response);
        }

        // Cập nhật số lượng hóa đơn chi tiết
        hoaDonChiTiet.setSoLuong(soLuong);
        hoaDonChiTiet.setThanhTien(sanPhamChiTiet.getGia().floatValue() * soLuong);
        hoaDonChiTietRepository.save(hoaDonChiTiet);

        // Cập nhật tồn kho nếu số lượng thay đổi
        sanPhamChiTiet.setSoLuong(soLuongTonKho - chenhLechSoLuong);
        sanPhamChiTiet.setTrangThai(sanPhamChiTiet.getSoLuong() == 0 ? "Hết Hàng" : "Còn Hàng");
        sanPhamChiTietRepository.save(sanPhamChiTiet);

        // Cập nhật tồn kho sản phẩm chính
        updateStockForProduct(sanPhamChiTiet.getSanPham());

        // Kiểm tra hóa đơn có tồn tại không
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            response.put("errorMessage", "Hóa đơn không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        // Cập nhật tổng tiền hóa đơn
        updateInvoiceTotal(hoaDonId);

        // Tính lại tổng tiền hóa đơn để áp dụng phiếu giảm giá
        float tongTienSanPham = (float) hoaDonChiTietRepository.findByHoaDonId(hoaDonId).stream()
                .mapToDouble(HoaDonChiTiet::getThanhTien)
                .sum();

        float phiShip = (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : 0;

        // Tìm và áp dụng phiếu giảm giá tốt nhất
        PhieuGiamGia bestVoucher = findBestVoucherForInvoice(tongTienSanPham);
        hoaDon.setPhieuGiamGia(bestVoucher);

        // Tính giá trị giảm giá từ phiếu (nếu có)
        float giaTriGiam = (bestVoucher != null && bestVoucher.getGiaTri() != null) ? bestVoucher.getGiaTri() : 0;

        // Đảm bảo tổng tiền không bị âm
        float tongTienFinal = Math.max(0, tongTienSanPham + phiShip - giaTriGiam);

        hoaDon.setTongTien(tongTienFinal);
        hoaDonRepository.save(hoaDon);

        // Trả về trạng thái hóa đơn
        response.put("successMessage", "Cập nhật số lượng sản phẩm thành công!");
        response.put("trangThai", hoaDon.getTrangThai());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> deleteProductOrder(Integer hoaDonChiTietId, Integer hoaDonId) {
        Map<String, String> response = new HashMap<>();

        // Tìm hóa đơn chi tiết cần xóa
        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(hoaDonChiTietId).orElse(null);
        if (hoaDonChiTiet == null) {
            response.put("errorMessage", "Hóa đơn chi tiết không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra hóa đơn có tồn tại không
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            response.put("errorMessage", "Hóa đơn không tồn tại.");
            return ResponseEntity.badRequest().body(response);
        }

        // Lấy thông tin sản phẩm chi tiết
        SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();
        if (sanPhamChiTiet != null) {
            // Hoàn trả lại số lượng sản phẩm về kho
            sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() + hoaDonChiTiet.getSoLuong());
            sanPhamChiTietRepository.save(sanPhamChiTiet);

            // Cập nhật số lượng tồn kho cho sản phẩm chính
            updateStockForProduct(sanPhamChiTiet.getSanPham());
        }

        // Xóa sản phẩm khỏi hóa đơn
        hoaDonChiTietRepository.delete(hoaDonChiTiet);

        // Cập nhật lại tổng tiền hóa đơn sau khi xóa sản phẩm
        float tongTienSanPham = hoaDon.getHoaDonChiTiet()
                .stream()
                .map(chiTiet -> chiTiet.getThanhTien())
                .reduce(0f, Float::sum);

        float phiShip = (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : 0;

        // Kiểm tra phiếu giảm giá tốt nhất dựa trên tổng tiền mới
        PhieuGiamGia bestVoucher = findBestVoucherForInvoice(tongTienSanPham);
        hoaDon.setPhieuGiamGia(bestVoucher);

        float giaTriGiam = (bestVoucher != null && bestVoucher.getGiaTri() != null) ? bestVoucher.getGiaTri() : 0;

        // Đảm bảo tổng tiền không bị âm
        float tongTienFinal = Math.max(0, tongTienSanPham + phiShip - giaTriGiam);
        hoaDon.setTongTien(tongTienFinal);

        // Kiểm tra nếu hóa đơn không còn sản phẩm nào -> cập nhật trạng thái "Đã hủy"
        if (hoaDon.getHoaDonChiTiet().isEmpty()) {
            hoaDon.setTrangThai("Đang Xử Lý");
        }

        hoaDonRepository.save(hoaDon);

        // Trả về phản hồi thành công
        response.put("successMessage", "Xóa sản phẩm khỏi hóa đơn thành công!");
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

    private void updateInvoiceTotal(Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            return; // Nếu hóa đơn không tồn tại, không cần cập nhật
        }

        // Tính tổng tiền sản phẩm từ danh sách hóa đơn chi tiết
        float tongTienSanPham = (float) hoaDonChiTietRepository.findByHoaDonId(hoaDonId).stream()
                .mapToDouble(HoaDonChiTiet::getThanhTien)
                .sum();

        // Lấy phí ship và kiểm tra null
        float phiShip = (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : 0;

        // Tìm phiếu giảm giá tốt nhất
        PhieuGiamGia bestVoucher = findBestVoucherForInvoice(tongTienSanPham);
        hoaDon.setPhieuGiamGia(bestVoucher);

        // Giá trị giảm giá từ phiếu (nếu có)
        float giaTriGiam = (bestVoucher != null && bestVoucher.getGiaTri() != null) ? bestVoucher.getGiaTri() : 0;

        // Đảm bảo tổng tiền không âm
        float tongTienFinal = Math.max(0, tongTienSanPham + phiShip - giaTriGiam);

        // Cập nhật tổng tiền cho hóa đơn
        hoaDon.setTongTien(tongTienFinal);
        hoaDonRepository.save(hoaDon);
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
        return hoaDonChiTietRepository.getTongSanPhamBanTrongThang("Đã Hoàn Thành");
    }
    public List<Object[]> getTotalSoldByProductInMonthWithImages() {
        return hoaDonChiTietRepository.getTotalSoldByProductInMonth("Đã Hoàn Thành"); // Trả về danh sách sản phẩm
    }

}
