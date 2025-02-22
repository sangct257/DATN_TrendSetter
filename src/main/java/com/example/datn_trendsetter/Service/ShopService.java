package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShopService {

    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private DiaChiRepository diaChiRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private PdfService pdfService;

    @Transactional
    public HoaDon createHoaDon(HoaDon hoaDon) throws Exception {
        if (hoaDon == null) {
            throw new Exception("Dữ liệu hóa đơn không được để trống.");
        }

        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getId() == null) {
            KhachHang khachHang = khachHangRepository.save(hoaDon.getKhachHang());
            hoaDon.setKhachHang(khachHang);
        }

        if (hoaDon.getNhanVien() != null && hoaDon.getNhanVien().getId() == null) {
            NhanVien nhanVien = nhanVienRepository.save(hoaDon.getNhanVien());
            hoaDon.setNhanVien(nhanVien);
        }

        // Kiểm tra và tạo phương thức thanh toán nếu chưa tồn tại
        createDefaultPaymentMethods();

        // Kiểm tra số lượng hóa đơn đang xử lý
        long countDangXuLy = hoaDonRepository.countByTrangThai("Đang xử lý");
        if (countDangXuLy >= 3) {
            throw new Exception("Đã đạt giới hạn 3 hóa đơn đang xử lý. Không thể tạo thêm.");
        }

        // Lấy phương thức thanh toán đầu tiên (nếu có)
        Optional<PhuongThucThanhToan> optionalPaymentMethod = phuongThucThanhToanRepository.findFirstByOrderByIdAsc();
        hoaDon.setPhuongThucThanhToan(optionalPaymentMethod.orElse(null)); // Cho phép null

        // Thiết lập thông tin hóa đơn
        hoaDon.setTongTien(0.0F);
        hoaDon.setPhiShip(0.0F);
        hoaDon.setLoaiHoaDon("Tại Quầy");
        hoaDon.setTrangThai("Đang Xử Lý");
        hoaDon.setNgayTao(LocalDateTime.now());

        // Lưu hóa đơn lần đầu để lấy ID
        hoaDon = hoaDonRepository.save(hoaDon);

        // Tạo mã hóa đơn ngẫu nhiên và cập nhật lại hóa đơn
        hoaDon.setMaHoaDon(generateUniqueMaHoaDon());
        hoaDonRepository.save(hoaDon);

        // Lưu lịch sử hóa đơn
        saveLichSuHoaDon(hoaDon);

        return hoaDon;
    }

    private void createDefaultPaymentMethods() {

        List<String> existingMethods = phuongThucThanhToanRepository.findAll().stream()
                .map(PhuongThucThanhToan::getTenPhuongThuc)
                .toList();

        List<PhuongThucThanhToan> newMethods = new ArrayList<>();

        if (!existingMethods.contains("Tiền Mặt")) {
            newMethods.add(new PhuongThucThanhToan("Tiền Mặt", "Thành Công", LocalDate.now(), null, null, null, false));
        }
        if (!existingMethods.contains("Chuyển Khoản")) {
            newMethods.add(new PhuongThucThanhToan("Chuyển Khoản", "Thành Công", LocalDate.now(), null, null, null, false));
        }

        if (!newMethods.isEmpty()) {
            phuongThucThanhToanRepository.saveAll(newMethods);
        }
    }

    private String generateUniqueMaHoaDon() {
        String maHoaDon;
        Random random = new Random();
        do {
            int randomNumber = 100000 + random.nextInt(900000);
            maHoaDon = "MH" + randomNumber;
        } while (hoaDonRepository.existsByMaHoaDon(maHoaDon));
        return maHoaDon;
    }

    private void saveLichSuHoaDon(HoaDon hoaDon) {
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setKhachHang(hoaDon.getKhachHang());
        lichSu.setNhanVien(hoaDon.getNhanVien());
        lichSu.setHanhDong(hoaDon.getTrangThai());
        lichSu.setNgayTao(LocalDateTime.now());
        lichSu.setNguoiTao(hoaDon.getNguoiTao());
        lichSu.setDeleted(false);
        lichSu.setGhiChu("Tạo mới hóa đơn, mã: " + hoaDon.getMaHoaDon());

        lichSuHoaDonRepository.save(lichSu);
    }

    @Transactional
    public void deleteHoaDon(Integer hoaDonId) {
        try {
            // Tìm hóa đơn theo id
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // Kiểm tra và xử lý các chi tiết hóa đơn
            if (hoaDon.getHoaDonChiTiet() != null && !hoaDon.getHoaDonChiTiet().isEmpty()) {
                for (HoaDonChiTiet hoaDonChiTiet : hoaDon.getHoaDonChiTiet()) {
                    // Hoàn trả lại số lượng tồn kho cho sản phẩm chi tiết
                    SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();
                    if (sanPhamChiTiet != null) {
                        sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() + hoaDonChiTiet.getSoLuong());
                        sanPhamChiTietRepository.save(sanPhamChiTiet);

                        // Cập nhật số lượng tồn kho cho sản phẩm chính
                        SanPham sanPham = sanPhamChiTiet.getSanPham();
                        updateStockForProduct(sanPham);
                    }
                }

                // Xóa các chi tiết hóa đơn
                hoaDonChiTietRepository.deleteAll(hoaDon.getHoaDonChiTiet());
            }

            // Xóa hóa đơn
            hoaDonRepository.delete(hoaDon);

        } catch (Exception e) {
            throw new RuntimeException("Xóa hóa đơn thất bại: " + e.getMessage());
        }
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


    // Các phương thức khác không thay đổi
    public void getHoaDonAndProducts(Integer hoaDonId, Model model) {
        // Lấy danh sách hóa đơn, sản phẩm, khách hàng
        List<HoaDon> hoaDons = hoaDonRepository.findByTrangThai("Đang Xử Lý");
        // Đếm sản phẩm cho từng hóa đơn
        for (HoaDon hoaDon : hoaDons) {
            int tongSanPham = hoaDon.getHoaDonChiTiet()
                    .stream()
                    .mapToInt(HoaDonChiTiet::getSoLuong)
                    .sum();
            hoaDon.setTongSanPham(tongSanPham);
        }
        model.addAttribute("hoaDons", hoaDons);

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
                if (!sp.getHinhAnh().isEmpty()) {  // Kiểm tra nếu sản phẩm có hình ảnh
                    String imageUrl = sp.getHinhAnh().get(0).getUrlHinhAnh(); // Lấy hình ảnh đầu tiên
                    if (!seenImages.contains(imageUrl)) {
                        seenImages.add(imageUrl);
                        uniqueSanPhamChiTiet.add(sp);
                    }
                }
            }

            // Trộn danh sách để có thứ tự ngẫu nhiên
            Collections.shuffle(uniqueSanPhamChiTiet);

            // Gán vào model
            model.addAttribute("sanPhamChiTiet", uniqueSanPhamChiTiet);

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

            model.addAttribute("hoaDonChiTiet", hoaDonChiTiet);
            model.addAttribute("hoaDon", hoaDon);
        }
    }

    public void getPageKhachHang(int pageSize,Model model) {
        Page<KhachHang> khachHangs = khachHangRepository.findAllByTrangThai("Đang Hoạt Động", Pageable.ofSize(pageSize));
        model.addAttribute("khachHangs", khachHangs);
    }

    public void getPageSanPhamChiTiet(int pageSize,Model model) {
        // Lấy tất cả sản phẩm có trạng thái "Còn Hàng"
        Page<SanPhamChiTiet> allSanPhamChiTietPage = sanPhamChiTietRepository.findByTrangThai("Còn Hàng", Pageable.ofSize(pageSize));
        List<SanPhamChiTiet> allSanPhamChiTiet = allSanPhamChiTietPage.getContent(); // Lấy danh sách sản phẩm từ Page

        // Sử dụng HashSet để đảm bảo không có sản phẩm bị trùng
        Set<SanPhamChiTiet> uniqueSanPhamChiTiet = new HashSet<>(allSanPhamChiTiet);

        // Trộn danh sách sản phẩm ngẫu nhiên
        List<SanPhamChiTiet> shuffledSanPhamChiTiet = new ArrayList<>(uniqueSanPhamChiTiet);
        Collections.shuffle(shuffledSanPhamChiTiet);

        // Chỉ lấy 5 sản phẩm ngẫu nhiên
        List<SanPhamChiTiet> sanPhamChiTiet = shuffledSanPhamChiTiet.stream().limit(5).collect(Collectors.toList());
        model.addAttribute("sanPhamChiTiet", sanPhamChiTiet);
    }

    //     Thêm khách hàng vào hóa đơn
    public String addCustomerToInvoice(Integer hoaDonId, Integer khachHangId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            throw new IllegalArgumentException("Hóa đơn không tồn tại!");
        }

        KhachHang khachHang = khachHangRepository.findById(khachHangId).orElse(null);
        if (khachHang == null) {
            throw new IllegalArgumentException("Khách hàng không tồn tại!");
        }

        // Thêm họ tên và số điện thoại khách hàng vào hóa đơn
        hoaDon.setNguoiNhan(khachHang.getHoTen());
        hoaDon.setSoDienThoai(khachHang.getSoDienThoai());
        hoaDon.setEmail(khachHang.getEmail());

        // Tìm địa chỉ mặc định của khách hàng
        DiaChi diaChi = diaChiRepository.findByKhachHangAndTrangThai(khachHang, "Mặc Định");
        if (diaChi != null) {
            hoaDon.setSoNha(diaChi.getSoNha());
            hoaDon.setTenDuong(diaChi.getTenDuong());
            hoaDon.setPhuong(diaChi.getPhuong());
            hoaDon.setHuyen(diaChi.getHuyen());
            hoaDon.setThanhPho(diaChi.getThanhPho());

            // Cập nhật phí ship dựa trên địa chỉ mới
            hoaDon.setPhiShip(tinhPhiShip(diaChi.getThanhPho(), diaChi.getHuyen()));
        }

        hoaDon.setKhachHang(khachHang);
        hoaDonRepository.save(hoaDon);

        return "Thông tin khách hàng đã được thêm vào hóa đơn!";
    }

    //     Xóa khách hàng vào hóa đơn
    public Map<String, String> deleteCustomerToInvoice(Integer hoaDonId) {
        Map<String, String> response = new HashMap<>();
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);

        if (hoaDon == null) {
            response.put("error", " Hóa đơn không tồn tại!");
            return response;
        }

        // Xóa thông tin khách hàng khỏi hóa đơn
        hoaDon.setNguoiNhan(null);
        hoaDon.setSoDienThoai(null);
        hoaDon.setSoNha(null);
        hoaDon.setTenDuong(null);
        hoaDon.setPhuong(null);
        hoaDon.setHuyen(null);
        hoaDon.setThanhPho(null);
        hoaDon.setKhachHang(null);
        hoaDon.setEmail(null);
        hoaDon.setPhiShip(0.0F);

        // Lưu hóa đơn
        hoaDonRepository.save(hoaDon);

        response.put("message", " Đã xóa khách hàng khỏi hóa đơn!");
        return response;
    }

    public ResponseEntity<Map<String,String>> handleProductOrder(String action, Integer sanPhamChiTietId, Integer hoaDonId, Integer soLuong,
                                                     Integer hoaDonChiTietId) {
        Map<String,String> response = new HashMap<>();
        switch (action) {
            case "add":
                return hoaDonChiTietService.addProductDetailToHoaDon(sanPhamChiTietId, hoaDonId, soLuong);
            case "update":
                return hoaDonChiTietService.updateQuantityOrder(hoaDonChiTietId, soLuong, hoaDonId);
            case "delete":
                return hoaDonChiTietService.deleteProductOrder(hoaDonChiTietId, hoaDonId);
            default:
                response.put("successMessage","Hành động không hợp lệ");
                return ResponseEntity.badRequest().body(response);
        }
    }


    public Map<String, String> updatePaymentMethod(Integer hoaDonId, Integer phuongThucThanhToanId) {
        // Lấy hóa đơn từ cơ sở dữ liệu
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            throw new IllegalArgumentException("Hóa đơn không tồn tại");
        }

        // Lấy phương thức thanh toán từ cơ sở dữ liệu
        PhuongThucThanhToan phuongThucThanhToan = phuongThucThanhToanRepository.findById(phuongThucThanhToanId).orElse(null);
        if (phuongThucThanhToan == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không tồn tại");
        }

        // Cập nhật phương thức thanh toán cho hóa đơn
        hoaDon.setPhuongThucThanhToan(phuongThucThanhToan);
        hoaDonRepository.save(hoaDon);

        // Trả về thông báo thành công và tên phương thức thanh toán đã được cập nhật
        return Map.of("success", "Phương thức thanh toán đã được cập nhật thành công!",
                "updatedPaymentMethod", phuongThucThanhToan.getTenPhuongThuc());
    }

    public String applyPhieuGiamGia(Integer hoaDonId, String tenChuongTrinh) {
        // Lấy hóa đơn từ cơ sở dữ liệu
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        // Lấy phiếu giảm giá dựa trên tên chương trình
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findByTenChuongTrinh(tenChuongTrinh)
                .orElseThrow(() -> new RuntimeException("Phiếu giảm giá không hợp lệ"));

        // Kiểm tra điều kiện phiếu giảm giá (tổng tiền phải lớn hơn hoặc bằng điều kiện)
        if (hoaDon.getTongTien() >= phieuGiamGia.getDieuKien()) {
            // Cập nhật phiếu giảm giá cho hóa đơn
            hoaDon.setPhieuGiamGia(phieuGiamGia);
            hoaDonRepository.save(hoaDon);
            return "Phiếu giảm giá đã được áp dụng!";
        } else {
            throw new RuntimeException("Điều kiện phiếu giảm giá không thỏa mãn.");
        }
    }


    @Transactional
    public String updateShippingAddress(Integer hoaDonId, String nguoiNhan, String soDienThoai, Integer soNha,
                                        String tenDuong, String phuong, String huyen, String thanhPho, String ghiChu) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        hoaDon.setNguoiNhan(nguoiNhan);
        hoaDon.setSoDienThoai(soDienThoai);
        hoaDon.setSoNha(soNha);
        hoaDon.setTenDuong(tenDuong);
        hoaDon.setPhuong(phuong);
        hoaDon.setHuyen(huyen);
        hoaDon.setThanhPho(thanhPho);
        hoaDon.setLoaiHoaDon("Giao Hàng");
        hoaDon.setGhiChu(ghiChu);

        // Tính phí ship
        float phiShip = 100000.0F;
        if ("Hà Nội".equalsIgnoreCase(thanhPho) || "Hồ Chí Minh".equalsIgnoreCase(thanhPho)) {
            phiShip = 30000.0F;
            List<String> quanTrungTamHN = List.of("Quận Hoàn Kiếm", "Quận Ba Đình", "Quận Đống Đa", "Quận Hai Bà Trưng");
            List<String> quanTrungTamHCM = List.of("Quận 1", "Quận 3", "Quận 5", "Quận 10");

            if ((thanhPho.equalsIgnoreCase("Hà Nội") && quanTrungTamHN.contains(huyen)) ||
                    (thanhPho.equalsIgnoreCase("Hồ Chí Minh") && quanTrungTamHCM.contains(huyen))) {
                phiShip = 20000.0F;
            }
        }

        hoaDon.setPhiShip(phiShip);
        hoaDonRepository.save(hoaDon);

        return "Địa chỉ được cập nhật thành công!";
    }


    public String confirmPayment(Integer hoaDonId, RedirectAttributes redirectAttributes) {
        try {
            // Lấy hóa đơn từ cơ sở dữ liệu
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // Kiểm tra nếu hóa đơn chi tiết không tồn tại
            if (hoaDon.getHoaDonChiTiet() == null || hoaDon.getHoaDonChiTiet().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chưa có sản phẩm sao thanh toán má!");
                return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
            }

            // Kiểm tra nếu phương thức thanh toán chưa được chọn
            if (hoaDon.getPhuongThucThanhToan() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chưa chọn phương thức thanh toán sao thanh toán má!");
                return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
            }

            // Kiểm tra nếu khách hàng có nhập thông tin giao hàng
            boolean hasReceiverInfo = hoaDon.getNguoiNhan() != null && !hoaDon.getNguoiNhan().isEmpty() &&
                    hoaDon.getSoDienThoai() != null && !hoaDon.getSoDienThoai().isEmpty();

            boolean missingAddress = hoaDon.getPhuong() == null || hoaDon.getPhuong().isEmpty() ||
                    hoaDon.getHuyen() == null || hoaDon.getHuyen().isEmpty() ||
                    hoaDon.getThanhPho() == null || hoaDon.getThanhPho().isEmpty() ||
                    hoaDon.getSoNha() == null || hoaDon.getSoNha() <= 0;

            if (hasReceiverInfo && missingAddress) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn chưa nhập địa chỉ giao hàng!");
                return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
            }

            // Xác định loại hóa đơn và trạng thái
            if (hasReceiverInfo) {
                hoaDon.setLoaiHoaDon("Giao Hàng");
                hoaDon.setLoaiGiaoDich("Trả Sau");

                // Kiểm tra nếu phí ship chưa được cập nhật
                if (hoaDon.getPhiShip() == null || hoaDon.getPhiShip() == 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Hãy cập nhật phí ship trước khi xác nhận thanh toán!");
                    return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
                }

                // Thiết lập thời gian nhận dự kiến (ví dụ: sau 3 ngày)
                hoaDon.setThoiGianNhanDuKien(LocalDate.now().plusDays(3));
                hoaDon.setTrangThai("Chờ Xác Nhận");
            } else {
                hoaDon.setLoaiHoaDon("Tại Quầy");
                hoaDon.setTrangThai("Đã Hoàn Thành");
            }

            // Tính tổng tiền
            float tongTien = hoaDon.getTongTien() + hoaDon.getPhiShip();
            if (hoaDon.getPhieuGiamGia() != null) {
                tongTien -= hoaDon.getPhieuGiamGia().getGiaTri();
            }
            hoaDon.setTongTien(tongTien);

            // Cập nhật thời gian sửa đổi hóa đơn
            hoaDon.setNgaySua(LocalDateTime.now());
            hoaDon.setGhiChu(hoaDon.getGhiChu());
            // Lưu hóa đơn vào cơ sở dữ liệu
            hoaDonRepository.save(hoaDon);


            // Tạo lịch sử hóa đơn
            LichSuHoaDon lichSuHoaDon = new LichSuHoaDon();
            lichSuHoaDon.setHoaDon(hoaDon);
            lichSuHoaDon.setKhachHang(hoaDon.getKhachHang());
            lichSuHoaDon.setNhanVien(hoaDon.getNhanVien());
            lichSuHoaDon.setHanhDong(hoaDon.getTrangThai());
            lichSuHoaDon.setNgayTao(LocalDateTime.now());
            lichSuHoaDon.setNguoiTao(hoaDon.getNguoiTao());
            lichSuHoaDon.setDeleted(false);
            lichSuHoaDon.setGhiChu("Hóa đơn: " + hoaDon.getMaHoaDon() + "chờ xác nhận");

            // Lưu lịch sử hóa đơn
            lichSuHoaDonRepository.save(lichSuHoaDon);

            // Chỉ tạo file PDF nếu là hóa đơn "Tại Quầy"
//            if ("Tại Quầy".equals(hoaDon.getLoaiHoaDon())) {
//                pdfService.generateInvoicePdf(hoaDon);
//            }


            // Xác định thông báo thành công
            String successMessage = hoaDon.getLoaiHoaDon().equals("Giao Hàng") ? "Chờ Xác Nhận!" : "Thanh toán thành công!";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

            // Redirect tùy theo loại hóa đơn
            if (hoaDon.getLoaiHoaDon().equals("Giao Hàng")) {
                return "redirect:/admin/order-details?hoaDonId=" + hoaDonId;
            } else {
                return "redirect:/admin/sell-counter";
            }
        } catch (Exception e) {
            // Xử lý lỗi nếu có
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        }
    }

    public Map<String, String> addNewCustomer(Integer hoaDonId, String nguoiNhan, String soDienThoai) {
        Map<String, String> response = new HashMap<>();
        try {
            // Kiểm tra nếu nguoiNhan và soDienThoai đều null thì không thực hiện
            if (nguoiNhan == null || nguoiNhan.trim().isEmpty() || soDienThoai == null || soDienThoai.trim().isEmpty()) {
                response.put("error", "Đã có thông tin người nhận!");
                return response;
            }

            // Lấy hóa đơn từ cơ sở dữ liệu
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // Cập nhật họ tên và số điện thoại vào hóa đơn
            hoaDon.setNguoiNhan(nguoiNhan);
            hoaDon.setSoDienThoai(soDienThoai);
            hoaDon.setSoNha(null);
            hoaDon.setPhuong(null);
            hoaDon.setHuyen(null);
            hoaDon.setThanhPho(null);
            hoaDon.setKhachHang(null);

            if (hoaDon.getThanhPho() == null) {
                hoaDon.setPhiShip(0.0F); // Nếu thành phố là null thì gán phí ship là 0
            } else if ("Hà Nội".equalsIgnoreCase(hoaDon.getThanhPho())) {
                hoaDon.setPhiShip(30000.0F); // Gán phí ship là 30k nếu thành phố là Hà Nội
            } else {
                hoaDon.setPhiShip(100000.0F); // Hoặc phí ship là 100k nếu không phải Hà Nội
            }

            // Lưu hóa đơn sau khi cập nhật
            hoaDonRepository.save(hoaDon);

            // Trả về thông báo thành công
            response.put("success", "Thông tin khách hàng đã được cập nhật!");
            return response;
        } catch (RuntimeException e) {
            // Trả về thông báo lỗi
            response.put("error", e.getMessage());
            return response;
        } catch (Exception e) {
            // Xử lý lỗi không mong muốn
            response.put("error", "Có lỗi xảy ra. Vui lòng thử lại.");
            return response;
        }
    }

    private float tinhPhiShip(String thanhPho, String huyen) {
        float phiShip = 100000.0F; // Mặc định phí ship tỉnh khác

        if ("Hà Nội".equalsIgnoreCase(thanhPho) || "Hồ Chí Minh".equalsIgnoreCase(thanhPho)) {
            phiShip = 30000.0F;

            // Nếu thuộc quận trung tâm, giảm phí ship
            List<String> quanTrungTamHN = List.of("Quận Hoàn Kiếm", "Quận Ba Đình", "Quận Đống Đa", "Quận Hai Bà Trưng");
            List<String> quanTrungTamHCM = List.of("Quận 1", "Quận 3", "Quận 5", "Quận 10");

            if ((thanhPho.equalsIgnoreCase("Hà Nội") && quanTrungTamHN.contains(huyen)) ||
                    (thanhPho.equalsIgnoreCase("Hồ Chí Minh") && quanTrungTamHCM.contains(huyen))) {
                phiShip = 20000.0F;
            }
        }

        return phiShip;
    }

}
