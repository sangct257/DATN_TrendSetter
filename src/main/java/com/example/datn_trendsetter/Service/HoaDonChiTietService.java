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
import java.util.*;
import java.util.stream.Collectors;

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

        // Kiểm tra số lượng tồn kho, nếu số lượng nhập vào lớn hơn tồn kho thì điều chỉnh lại số lượng
        if (soLuong > sanPhamChiTiet.getSoLuong()) {
            response.put("errorMessage", "Số lượng nhập vào vượt quá số lượng tồn kho!");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra hóa đơn tồn tại
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            response.put("errorMessage", "Hóa đơn không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra số lượng tồn kho hiện tại và tổng số lượng trong hóa đơn
        Optional<HoaDonChiTiet> chiTietTrungSP = hoaDonChiTietRepository.findByHoaDonIdAndSanPhamChiTietId(hoaDonId, sanPhamChiTietId);
        int tongSoLuongHienTai = chiTietTrungSP.stream().mapToInt(HoaDonChiTiet::getSoLuong).sum();
        int tongSoLuongSauThem = tongSoLuongHienTai + soLuong;

        // Kiểm tra lại số lượng tồn kho sau khi thêm sản phẩm
        if (tongSoLuongSauThem > sanPhamChiTiet.getSoLuong()) {
            response.put("errorMessage", "Tổng số lượng sản phẩm trong hóa đơn vượt quá số lượng tồn kho!");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra xem sản phẩm đã có trong hóa đơn chưa
        HoaDonChiTiet hoaDonChiTiet = chiTietTrungSP.isEmpty() ? new HoaDonChiTiet() : chiTietTrungSP.get();

        if (hoaDonChiTiet.getId() != null) {
            // Nếu sản phẩm đã có trong hóa đơn nhưng có giá khác, tạo bản ghi mới
            if (!sanPhamChiTiet.getGia().equals(hoaDonChiTiet.getGia())) {
                HoaDonChiTiet hoaDonChiTietMoi = new HoaDonChiTiet();
                hoaDonChiTietMoi.setHoaDon(hoaDon);
                hoaDonChiTietMoi.setSanPhamChiTiet(sanPhamChiTiet);
                hoaDonChiTietMoi.setSoLuong(soLuong);
                hoaDonChiTietMoi.setGia(sanPhamChiTiet.getGia().floatValue());
                hoaDonChiTietMoi.setThanhTien(soLuong * hoaDonChiTietMoi.getGia());

                hoaDonChiTietRepository.save(hoaDonChiTietMoi);
            } else {
                // Nếu giá không thay đổi, chỉ cập nhật số lượng
                int soLuongMoi = hoaDonChiTiet.getSoLuong() + soLuong;
                hoaDonChiTiet.setSoLuong(soLuongMoi);
                hoaDonChiTiet.setThanhTien(soLuongMoi * hoaDonChiTiet.getGia());
                hoaDonChiTietRepository.save(hoaDonChiTiet);
            }
        } else {
            // Nếu sản phẩm chưa có trong hóa đơn, tạo mới
            hoaDonChiTiet.setHoaDon(hoaDon);
            hoaDonChiTiet.setSanPhamChiTiet(sanPhamChiTiet);
            hoaDonChiTiet.setSoLuong(soLuong);
            hoaDonChiTiet.setGia(sanPhamChiTiet.getGia().floatValue());
            hoaDonChiTiet.setThanhTien(soLuong * hoaDonChiTiet.getGia());

            hoaDonChiTietRepository.save(hoaDonChiTiet);
        }

        // ✅ Nếu hóa đơn KHÔNG ở trạng thái "Chờ Xác Nhận" thì mới cập nhật tồn kho
        if (!"Chờ Xác Nhận".equalsIgnoreCase(hoaDon.getTrangThai())) {
            // Trừ kho
            sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() - soLuong);
            sanPhamChiTiet.setTrangThai(sanPhamChiTiet.getSoLuong() == 0 ? "Hết Hàng" : "Còn Hàng");
            sanPhamChiTietRepository.save(sanPhamChiTiet);

            // Cập nhật tồn kho sản phẩm chính
            updateStockForProduct(sanPhamChiTiet.getSanPham());
        }

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

        SanPham sanPham = sanPhamChiTiet.getSanPham();
        if (sanPham == null) {
            response.put("errorMessage", "Sản phẩm không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        if ("Ngừng Hoạt Động".equalsIgnoreCase(sanPham.getTrangThai())) {
            response.put("errorMessage", "Sản phẩm hiện đang ngừng hoạt động và không thể cập nhật số lượng!");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra xem giá sản phẩm chi tiết có khớp với giá của hóa đơn chi tiết không
        if (!sanPhamChiTiet.getGia().equals(hoaDonChiTiet.getGia())) {
            response.put("errorMessage", "Giá sản phẩm không khớp với giá của hóa đơn chi tiết!");
            return ResponseEntity.badRequest().body(response);
        }

        int soLuongTonKho = sanPhamChiTiet.getSoLuong();
        int soLuongCu = hoaDonChiTiet.getSoLuong();
        int chenhLechSoLuong = soLuong - soLuongCu;

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            response.put("errorMessage", "Hóa đơn không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        // Tính tổng số lượng của sản phẩm chi tiết tương tự trong hóa đơn
        int soLuongCuaTatCaChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDonId).stream()
                .filter(ct -> ct.getSanPhamChiTiet().equals(sanPhamChiTiet)) // Lọc những chi tiết hóa đơn có sản phẩm chi tiết giống nhau
                .mapToInt(HoaDonChiTiet::getSoLuong)
                .sum();

        // Kiểm tra số lượng tồn kho khi hóa đơn ở trạng thái "Chờ Xác Nhận"
        if ("Chờ Xác Nhận".equalsIgnoreCase(hoaDon.getTrangThai())) {
            if (soLuongCuaTatCaChiTiet + chenhLechSoLuong > soLuongTonKho) {
                response.put("errorMessage", "Tổng số lượng sản phẩm chi tiết trong hóa đơn vượt quá số lượng tồn kho!");
                return ResponseEntity.badRequest().body(response);
            }
        } else {
            // Đã trừ tồn kho => chỉ được tăng nếu còn hàng
            if (soLuongCuaTatCaChiTiet + chenhLechSoLuong > (soLuongTonKho + soLuongCu)) {
                response.put("errorMessage", "Số lượng cập nhật vượt quá tồn kho thực tế!");
                return ResponseEntity.badRequest().body(response);
            }
        }

        // Tính tổng tiền sau khi cập nhật số lượng sản phẩm
        float tongTienSanPhamSauCapNhat = (float) hoaDonChiTietRepository.findByHoaDonId(hoaDonId).stream()
                .filter(ct -> !ct.getId().equals(hoaDonChiTietId)) // Lọc bỏ chi tiết hóa đơn hiện tại
                .mapToDouble(HoaDonChiTiet::getThanhTien) // Tổng tiền của các chi tiết hóa đơn còn lại
                .sum()
                + (sanPhamChiTiet.getGia().floatValue() * soLuong); // Cộng thêm tiền sản phẩm đã cập nhật

        // Tính phí ship
        float phiShip = hoaDon.getPhiShip() != null ? hoaDon.getPhiShip() : 0;

        // Tính tổng tiền sau khi cập nhật số lượng và phí ship
        float tongTienSauCapNhat = tongTienSanPhamSauCapNhat + phiShip;

        // Lấy giá trị giảm từ phiếu giảm giá nếu có
        PhieuGiamGia phieuGiamGia = hoaDon.getPhieuGiamGia();
        float giaTriGiam = (phieuGiamGia != null) ? phieuGiamGia.getGiaTriGiam() : 0;

        // Cập nhật tổng tiền cuối cùng (sau khi áp dụng giảm giá)
        float tongTienFinal = Math.max(0, tongTienSauCapNhat - giaTriGiam);

        // Kiểm tra nếu tổng tiền đã thanh toán lớn hơn hoặc bằng tổng tiền mới
        double tongSoTienDaThanhToan = lichSuThanhToanRepository.findByHoaDonId(hoaDonId)
                .stream()
                .mapToDouble(LichSuThanhToan::getSoTienThanhToan)
                .sum();

        if (tongSoTienDaThanhToan > tongTienFinal) {
            response.put("errorMessage", "Không thể cập nhật vì tổng tiền đã thanh toán lớn hơn tổng tiền mới!");
            return ResponseEntity.badRequest().body(response);
        }

        hoaDonChiTiet.setSoLuong(soLuong);
        hoaDonChiTiet.setThanhTien(sanPhamChiTiet.getGia().floatValue() * soLuong);
        hoaDonChiTietRepository.save(hoaDonChiTiet);

        // ✅ Nếu hóa đơn KHÔNG ở trạng thái "Chờ Xác Nhận" thì mới cập nhật tồn kho
        if (!"Chờ Xác Nhận".equals(hoaDon.getTrangThai())) {
            sanPhamChiTiet.setSoLuong(soLuongTonKho - chenhLechSoLuong);
            sanPhamChiTiet.setTrangThai(sanPhamChiTiet.getSoLuong() == 0 ? "Hết Hàng" : "Còn Hàng");
            sanPhamChiTietRepository.save(sanPhamChiTiet);
        }

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
        // Hoàn trả số lượng sản phẩm nếu hóa đơn không ở trạng thái "Chờ Xác Nhận"
        if (!"Chờ Xác Nhận".equals(hoaDon.getTrangThai())) {
            SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();
            if (sanPhamChiTiet != null) {
                sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() + hoaDonChiTiet.getSoLuong());
                sanPhamChiTiet.setTrangThai(sanPhamChiTiet.getSoLuong() > 0 ? "Còn Hàng" : "Hết Hàng");
                sanPhamChiTietRepository.save(sanPhamChiTiet);
                updateStockForProduct(sanPhamChiTiet.getSanPham());
            }
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
        List<Object[]> rawList = hoaDonChiTietRepository.getTotalSoldByProductInMonth("Đã Hoàn Thành");
        return rawList.stream()
                .filter(item -> {
                    String trangThaiSanPham = (String) item[5]; // Cột 5 mới đúng sp.trangThai
                    return "Đang Hoạt Động".equals(trangThaiSanPham);
                })
                .collect(Collectors.toList());
    }



}