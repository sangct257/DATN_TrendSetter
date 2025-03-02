package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import jakarta.persistence.EntityNotFoundException;
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

            // Kiểm tra danh sách chi tiết hóa đơn
            if (hoaDon.getHoaDonChiTiet() != null && !hoaDon.getHoaDonChiTiet().isEmpty()) {
                for (HoaDonChiTiet hoaDonChiTiet : hoaDon.getHoaDonChiTiet()) {
                    if (hoaDonChiTiet == null) continue; // Nếu có chi tiết null, bỏ qua

                    // Lấy sản phẩm chi tiết
                    SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();
                    if (sanPhamChiTiet != null) {
                        Integer soLuongChiTiet = hoaDonChiTiet.getSoLuong();
                        if (soLuongChiTiet != null) {
                            sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() + soLuongChiTiet);
                            sanPhamChiTietRepository.save(sanPhamChiTiet);
                        }

                        // Lấy sản phẩm chính
                        SanPham sanPham = sanPhamChiTiet.getSanPham();
                        if (sanPham != null) {
                            updateStockForProduct(sanPham);
                        }
                    }
                }

                // Xóa các chi tiết hóa đơn
                hoaDonChiTietRepository.deleteAll(hoaDon.getHoaDonChiTiet());
            }

            // Xóa lịch sử hóa đơn nếu có
            if (hoaDon.getLichSuHoaDon() != null && !hoaDon.getLichSuHoaDon().isEmpty()) {
                lichSuHoaDonRepository.deleteAll(hoaDon.getLichSuHoaDon());
            }

            // Xóa hóa đơn
            hoaDonRepository.delete(hoaDon);

        } catch (Exception e) {
            throw new RuntimeException("Xóa hóa đơn thất bại: " + e.getMessage());
        }
    }

    // Phương thức cập nhật tồn kho cho sản phẩm chính
    private void updateStockForProduct(SanPham sanPham) {
        if (sanPham == null) return; // Nếu sản phẩm chính bị null, không làm gì cả

        List<SanPhamChiTiet> listSanPhamChiTiet = sanPhamChiTietRepository.findBySanPham(sanPham);
        if (listSanPhamChiTiet == null || listSanPhamChiTiet.isEmpty()) {
            sanPham.setSoLuong(0); // Nếu không có sản phẩm chi tiết, tồn kho về 0
        } else {
            int tongSoLuong = listSanPhamChiTiet.stream()
                    .filter(spct -> spct.getSoLuong() != null)
                    .mapToInt(SanPhamChiTiet::getSoLuong)
                    .sum();
            sanPham.setSoLuong(tongSoLuong);
        }
        sanPhamRepository.save(sanPham);
    }


    // Các phương thức khác không thay đổi
    public void getHoaDonAndProducts(Integer hoaDonId, Model model) {
        // Lấy danh sách hóa đơn có trạng thái "Đang Xử Lý"
        List<HoaDon> hoaDons = hoaDonRepository.findByTrangThai("Đang Xử Lý");

        // Đếm tổng số sản phẩm cho từng hóa đơn
        for (HoaDon hoaDon : hoaDons) {
            if (hoaDon.getHoaDonChiTiet() != null) { // Tránh NullPointerException
                int tongSanPham = hoaDon.getHoaDonChiTiet()
                        .stream()
                        .mapToInt(HoaDonChiTiet::getSoLuong)
                        .sum();
                hoaDon.setTongSanPham(tongSanPham);
            }
        }
        model.addAttribute("hoaDons", hoaDons);

        // Nếu có hoaDonId, lấy chi tiết hóa đơn và sản phẩm
        if (hoaDonId != null) {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
            if (hoaDon == null) return; // Tránh lỗi nếu không tìm thấy hóa đơn

            List<HoaDonChiTiet> hoaDonChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            model.addAttribute("hoaDonChiTiet", hoaDonChiTiet);
            model.addAttribute("hoaDon", hoaDon);

            // Lấy sản phẩm chi tiết có trạng thái "Còn Hàng" và lọc theo hình ảnh duy nhất
            List<SanPhamChiTiet> allSanPhamChiTiet = sanPhamChiTietRepository.findByTrangThai("Còn Hàng");

            Set<String> seenImages = new HashSet<>();
            List<SanPhamChiTiet> uniqueSanPhamChiTiet = new ArrayList<>();

            for (SanPhamChiTiet sp : allSanPhamChiTiet) {
                List<HinhAnh> hinhAnhs = sp.getHinhAnh();
                if (hinhAnhs != null && !hinhAnhs.isEmpty()) { // Tránh lỗi IndexOutOfBoundsException
                    String imageUrl = hinhAnhs.get(0).getUrlHinhAnh();
                    if (seenImages.add(imageUrl)) { // Chỉ thêm sản phẩm nếu ảnh chưa xuất hiện
                        uniqueSanPhamChiTiet.add(sp);
                    }
                }
            }

            Collections.shuffle(uniqueSanPhamChiTiet);
            model.addAttribute("sanPhamChiTiet", uniqueSanPhamChiTiet);

            // Lấy danh sách khách hàng và phương thức thanh toán
            Page<KhachHang> khachHangs = khachHangRepository.findAllByTrangThai("Đang Hoạt Động", Pageable.ofSize(5));
            List<PhuongThucThanhToan> listPhuongThucThanhToan = phuongThucThanhToanRepository.findAll();
            model.addAttribute("khachHangs", khachHangs);
            model.addAttribute("listPhuongThucThanhToan", listPhuongThucThanhToan);

            // Lọc danh sách phiếu giảm giá dựa trên tổng tiền
            Float tongTien = hoaDon.getTongTien();
            List<PhieuGiamGia> validPhieuGiamGia = (tongTien != null)
                    ? phieuGiamGiaRepository.findByDieuKienLessThanEqual(tongTien) // Dùng truy vấn thay vì filter
                    : new ArrayList<>();

            model.addAttribute("listPhieuGiamGia", validPhieuGiamGia);
        }
    }

    //     Thêm khách hàng vào hóa đơn
    public String addCustomerToInvoice(Integer hoaDonId, Integer khachHangId) {
        // Kiểm tra đầu vào hợp lệ
        if (hoaDonId == null || khachHangId == null) {
            throw new IllegalArgumentException("ID hóa đơn và ID khách hàng không được null");
        }

        // Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElseThrow(
                () -> new IllegalArgumentException("Hóa đơn không tồn tại!"));

        // Kiểm tra nếu hóa đơn đã có khách hàng
        if (hoaDon.getKhachHang() != null) {
            throw new IllegalStateException("Hóa đơn này đã có khách hàng, không thể thêm mới!");
        }

        // Tìm khách hàng
        KhachHang khachHang = khachHangRepository.findById(khachHangId)
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại!"));

        // Kiểm tra thông tin khách hàng có đầy đủ không
        if (khachHang.getHoTen() == null || khachHang.getSoDienThoai() == null || khachHang.getEmail() == null) {
            throw new IllegalArgumentException("Thông tin khách hàng không đầy đủ!");
        }

        // Cập nhật thông tin khách hàng vào hóa đơn
        hoaDon.setKhachHang(khachHang);
        hoaDon.setNguoiNhan(khachHang.getHoTen());
        hoaDon.setSoDienThoai(khachHang.getSoDienThoai());
        hoaDon.setEmail(khachHang.getEmail());

        // Tìm địa chỉ mặc định của khách hàng
        DiaChi diaChi = diaChiRepository.findByKhachHangAndTrangThai(khachHang, "Mặc Định");
        float phiShip = (diaChi != null) ? tinhPhiShip(diaChi.getThanhPho(), diaChi.getHuyen()) : 0F;

        if (diaChi != null) {
            hoaDon.setSoNha(diaChi.getSoNha());
            hoaDon.setTenDuong(diaChi.getTenDuong());
            hoaDon.setPhuong(diaChi.getPhuong());
            hoaDon.setHuyen(diaChi.getHuyen());
            hoaDon.setThanhPho(diaChi.getThanhPho());
        }

        hoaDon.setPhiShip(phiShip);

        // Tính tổng tiền hóa đơn
        float tongTienSanPham = hoaDon.getHoaDonChiTiet().stream()
                .map(HoaDonChiTiet::getThanhTien)
                .filter(Objects::nonNull)
                .reduce(0F, Float::sum);

        float giaTriGiamGia = (hoaDon.getPhieuGiamGia() != null && hoaDon.getPhieuGiamGia().getGiaTri() != null)
                ? hoaDon.getPhieuGiamGia().getGiaTri() : 0F;

        hoaDon.setTongTien(Math.max(tongTienSanPham + phiShip - giaTriGiamGia, 0));

        // Lưu hóa đơn vào cơ sở dữ liệu
        hoaDonRepository.save(hoaDon);
        return "Thông tin khách hàng đã được thêm vào hóa đơn!";
    }

    public Map<String, String> deleteCustomerToInvoice(Integer hoaDonId) {
        Map<String, String> response = new HashMap<>();

        // Kiểm tra ID hóa đơn hợp lệ
        if (hoaDonId == null) {
            response.put("error", "ID hóa đơn không được null!");
            return response;
        }

        // Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            response.put("error", "Hóa đơn không tồn tại!");
            return response;
        }

        // Kiểm tra xem hóa đơn có khách hàng không
        if (hoaDon.getKhachHang() == null) {
            // Nếu không có khách hàng, set null cho các trường thông tin khách hàng
            hoaDon.setNguoiNhan(null);
            hoaDon.setSoDienThoai(null);
            hoaDon.setEmail(null); // Nếu bạn cũng muốn xóa email
            hoaDon.setSoNha(null);
            hoaDon.setTenDuong(null);
            hoaDon.setPhuong(null);
            hoaDon.setHuyen(null);
            hoaDon.setThanhPho(null);
            hoaDon.setPhiShip(0.0F); // Đặt phí ship về 0
        } else {
            // Nếu có khách hàng, kiểm tra trạng thái hóa đơn
            if ("Đã Hoàn Thành".equalsIgnoreCase(hoaDon.getTrangThai()) || "Đang Giao Hàng".equalsIgnoreCase(hoaDon.getTrangThai())) {
                response.put("error", "Không thể xóa khách hàng khỏi hóa đơn đã thanh toán hoặc Đang Giao Hàng!");
                return response;
            }

            // Xóa thông tin khách hàng khỏi hóa đơn
            hoaDon.setNguoiNhan(null);
            hoaDon.setSoDienThoai(null);
            hoaDon.setEmail(null);
            hoaDon.setSoNha(null);
            hoaDon.setTenDuong(null);
            hoaDon.setPhuong(null);
            hoaDon.setHuyen(null);
            hoaDon.setThanhPho(null);
            hoaDon.setPhiShip(0.0F);

            // Set null cho khách hàng
            hoaDon.setKhachHang(null);
        }

        // Tính tổng tiền hóa đơn
        float tongTienSanPham = hoaDon.getHoaDonChiTiet().stream()
                .map(HoaDonChiTiet::getThanhTien)
                .filter(Objects::nonNull)
                .reduce(0F, Float::sum);

        float giaTriGiamGia = (hoaDon.getPhieuGiamGia() != null && hoaDon.getPhieuGiamGia().getGiaTri() != null)
                ? hoaDon.getPhieuGiamGia().getGiaTri() : 0F;

        float phiShip = (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : 0F;

        // Áp dụng công thức: tổng thành tiền trong hóa đơn chi tiết - phí ship - giá trị phiếu giảm giá
        hoaDon.setTongTien(Math.max(tongTienSanPham - phiShip - giaTriGiamGia, 0));

        // Lưu hóa đơn vào cơ sở dữ liệu
        hoaDonRepository.save(hoaDon);

        response.put("message", "Đã xóa khách hàng khỏi hóa đơn!");
        return response;
    }

    @Transactional
    public String updateShippingAddress(Integer hoaDonId, String nguoiNhan, String soDienThoai, Integer soNha,
                                        String tenDuong, String phuong, String huyen, String thanhPho, String ghiChu) {
        // Kiểm tra ID hóa đơn hợp lệ
        if (hoaDonId == null) {
            throw new IllegalArgumentException("ID hóa đơn không được null!");
        }

        // Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new EntityNotFoundException("Hóa đơn không tồn tại!"));

        // Kiểm tra trạng thái hóa đơn
        if ("Đã Hoàn Thành".equalsIgnoreCase(hoaDon.getTrangThai()) ||
                "Đang Vận Chuyển".equalsIgnoreCase(hoaDon.getTrangThai())) {
            throw new IllegalStateException("Không thể cập nhật địa chỉ khi hóa đơn đã thanh toán hoặc đang vận chuyển!");
        }

//        // Kiểm tra dữ liệu đầu vào
//        if (nguoiNhan == null || nguoiNhan.isBlank() ||
//                soDienThoai == null || soDienThoai.isBlank() ||
//                soNha == null || thanhPho == null || thanhPho.isBlank() ||
//                huyen == null || huyen.isBlank()) {
//            throw new IllegalArgumentException("Thông tin địa chỉ không hợp lệ!");
//        }


        // Xóa địa chỉ cũ
        hoaDon.setNguoiNhan(null);
        hoaDon.setSoDienThoai(null);
        hoaDon.setSoNha(null);
        hoaDon.setTenDuong(null);
        hoaDon.setPhuong(null);
        hoaDon.setHuyen(null);
        hoaDon.setThanhPho(null);
        hoaDon.setGhiChu(null);

        // Cập nhật địa chỉ mới
        hoaDon.setNguoiNhan(nguoiNhan);
        hoaDon.setSoDienThoai(soDienThoai);
        hoaDon.setSoNha(soNha);
        hoaDon.setTenDuong(tenDuong);
        hoaDon.setPhuong(phuong);
        hoaDon.setHuyen(huyen);
        hoaDon.setThanhPho(thanhPho);
        hoaDon.setLoaiHoaDon("Giao Hàng");
        hoaDon.setGhiChu(ghiChu);

        // Tính phí ship mới
        float phiShipMoi = tinhPhiShip(thanhPho, huyen);
        hoaDon.setPhiShip(phiShipMoi);

        // Tính tổng tiền hóa đơn
        float tongTienSanPham = hoaDon.getHoaDonChiTiet().stream()
                .map(HoaDonChiTiet::getThanhTien)
                .filter(Objects::nonNull)
                .reduce(0F, Float::sum);

        float giaTriGiamGia = (hoaDon.getPhieuGiamGia() != null && hoaDon.getPhieuGiamGia().getGiaTri() != null)
                ? hoaDon.getPhieuGiamGia().getGiaTri() : 0F;

        hoaDon.setTongTien(Math.max(tongTienSanPham + phiShipMoi - giaTriGiamGia, 0));

        // Lưu hóa đơn vào cơ sở dữ liệu
        hoaDonRepository.save(hoaDon);

        return "Địa chỉ được cập nhật thành công!";
    }

    // Phương thức tính phí ship
    private float tinhPhiShip(String thanhPho, String huyen) {
        // Nếu không có thông tin thành phố hoặc huyện, phí ship = 0
        if (thanhPho == null || thanhPho.isEmpty() || huyen == null || huyen.isEmpty()) {
            return 0.0F;
        }

        float phiShip = 100000.0F; // Mặc định phí ship tỉnh khác

        if ("Hà Nội".equalsIgnoreCase(thanhPho) || "Hồ Chí Minh".equalsIgnoreCase(thanhPho)) {
            phiShip = 30000.0F;
            List<String> quanTrungTamHN = List.of("Quận Hoàn Kiếm", "Quận Ba Đình", "Quận Đống Đa", "Quận Hai Bà Trưng");
            List<String> quanTrungTamHCM = List.of("Quận 1", "Quận 3", "Quận 5", "Quận 10");

            if (("Hà Nội".equalsIgnoreCase(thanhPho) && quanTrungTamHN.contains(huyen)) ||
                    ("Hồ Chí Minh".equalsIgnoreCase(thanhPho) && quanTrungTamHCM.contains(huyen))) {
                phiShip = 20000.0F;
            }
        }

        return phiShip;
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


    @Transactional
    public String applyPhieuGiamGia(Integer hoaDonId, String tenChuongTrinh) {
        // Lấy hóa đơn từ cơ sở dữ liệu
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        // Kiểm tra nếu hóa đơn đã thanh toán
        if ("Đã Hoàn Thành".equalsIgnoreCase(hoaDon.getTrangThai())) {
            throw new RuntimeException("Không thể thay đổi phiếu giảm giá sau khi thanh toán.");
        }

        // Lấy phiếu giảm giá dựa trên tên chương trình
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findByTenChuongTrinh(tenChuongTrinh)
                .orElseThrow(() -> new RuntimeException("Phiếu giảm giá không hợp lệ"));

        // Kiểm tra hạn sử dụng
        LocalDateTime now = LocalDateTime.now();
        if (phieuGiamGia.getNgayBatDau().isAfter(now)) {
            throw new RuntimeException("Phiếu giảm giá chưa đến thời gian áp dụng.");
        }
        if (phieuGiamGia.getNgayKetThuc().isBefore(now)) {
            throw new RuntimeException("Phiếu giảm giá đã hết hạn.");
        }

        // Kiểm tra điều kiện tổng tiền
        if (hoaDon.getTongTien() < phieuGiamGia.getDieuKien()) {
            throw new RuntimeException("Điều kiện phiếu giảm giá không thỏa mãn.");
        }

        // Kiểm tra loại hóa đơn có phù hợp không (nếu áp dụng cho giao hàng)
        if ("Giao Hàng".equalsIgnoreCase(phieuGiamGia.getLoaiApDung()) &&
                !"Giao Hàng".equalsIgnoreCase(hoaDon.getLoaiHoaDon())) {
            throw new RuntimeException("Phiếu giảm giá chỉ áp dụng cho đơn hàng giao hàng.");
        }

        hoaDon.setPhieuGiamGia(phieuGiamGia);

        // Tính lại tổng tiền sau khi áp dụng phiếu giảm giá
        float tongTienSanPham = hoaDon.getHoaDonChiTiet().stream()
                .map(HoaDonChiTiet::getThanhTien)
                .filter(Objects::nonNull)
                .reduce(0F, Float::sum);

        float phiShip = (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : 0F;
        float tongTienMoi = Math.max(tongTienSanPham + phiShip - phieuGiamGia.getGiaTri(), 0);
        hoaDon.setTongTien(tongTienMoi);

        // Lưu hóa đơn
        hoaDonRepository.save(hoaDon);
        phieuGiamGiaRepository.save(phieuGiamGia);

        return "Phiếu giảm giá đã được áp dụng!";
    }


    public String confirmPayment(Integer hoaDonId, RedirectAttributes redirectAttributes) {
        try {
            // Lấy hóa đơn từ cơ sở dữ liệu
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // Kiểm tra nếu hóa đơn không có sản phẩm
            if (hoaDon.getHoaDonChiTiet() == null || hoaDon.getHoaDonChiTiet().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chưa có sản phẩm sao thanh toán má!");
                return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
            }

            // Kiểm tra nếu phương thức thanh toán chưa được chọn
            if (hoaDon.getPhuongThucThanhToan() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chưa chọn phương thức thanh toán sao thanh toán má!");
                return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
            }

            // Kiểm tra thông tin giao hàng
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

            // Xác định loại hóa đơn
            if (hasReceiverInfo) {
                hoaDon.setLoaiHoaDon("Giao Hàng");

                // Kiểm tra phương thức thanh toán
                if (hoaDon.getPhuongThucThanhToan() != null && hoaDon.getPhuongThucThanhToan().getTenPhuongThuc() != null) {
                    if ("Tiền Mặt".equalsIgnoreCase(hoaDon.getPhuongThucThanhToan().getTenPhuongThuc())) {
                        hoaDon.setLoaiGiaoDich(hoaDon.getLoaiGiaoDich());
                    } else if ("Chuyển Khoản".equalsIgnoreCase(hoaDon.getPhuongThucThanhToan().getTenPhuongThuc())) {
                        hoaDon.setLoaiGiaoDich(hoaDon.getLoaiGiaoDich());
                    }
                } else {
                    // Xử lý trường hợp không có phương thức thanh toán
                    redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn phương thức thanh toán trước khi xác nhận!");
                    return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
                }

                // Kiểm tra phí ship
                if (hoaDon.getPhiShip() == null || hoaDon.getPhiShip() <= 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Hãy cập nhật phí ship trước khi xác nhận thanh toán!");
                    return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
                }

                // Cập nhật trạng thái và ngày giao dự kiến
                hoaDon.setThoiGianNhanDuKien(LocalDate.now().plusDays(3));
                hoaDon.setTrangThai("Chờ Xác Nhận");
            } else {
                hoaDon.setLoaiHoaDon("Tại Quầy");
                hoaDon.setTrangThai("Đã Hoàn Thành");
            }


            // Tính tổng tiền: Tổng thành tiền của hóa đơn chi tiết
            Float tongTien = (float) hoaDon.getHoaDonChiTiet().stream()
                    .mapToDouble(HoaDonChiTiet::getThanhTien)
                    .sum();

            // Cộng phí ship nếu không null
            tongTien += (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : 0;

            // Trừ giá trị phiếu giảm giá nếu không null
            tongTien -= (hoaDon.getPhieuGiamGia() != null && hoaDon.getPhieuGiamGia().getGiaTri() != null) ? hoaDon.getPhieuGiamGia().getGiaTri() : 0;

            hoaDon.setTongTien(tongTien);

            // Cập nhật thời gian sửa đổi hóa đơn
            hoaDon.setNgaySua(LocalDateTime.now());

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
            lichSuHoaDon.setGhiChu("Hóa đơn: " + hoaDon.getMaHoaDon() + " " + hoaDon.getTrangThai());

            // Lưu lịch sử hóa đơn
            lichSuHoaDonRepository.save(lichSuHoaDon);

            // Xác định thông báo thành công
            String successMessage = hoaDon.getLoaiHoaDon().equals("Giao Hàng") ? "Chờ Xác Nhận!" : "Thanh toán thành công!";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

            // Redirect tùy theo loại hóa đơn
            if ("Giao Hàng".equals(hoaDon.getLoaiHoaDon())) {
                return "redirect:/admin/order-details?hoaDonId=" + hoaDonId;
            } else {
                return "redirect:/admin/sell-counter";
            }
        } catch (Exception e) {
            // Xử lý lỗi nếu có
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
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
            hoaDon.setTenDuong(null);
            hoaDon.setPhuong(null);
            hoaDon.setHuyen(null);
            hoaDon.setThanhPho(null);
            hoaDon.setKhachHang(null);

            // Cập nhật phí ship dựa trên thành phố và huyện/quận
            hoaDon.setPhiShip(tinhPhiShip(hoaDon.getThanhPho(), hoaDon.getHuyen()));

            // Lưu hóa đơn sau khi cập nhật
            hoaDonRepository.save(hoaDon);

            // Trả về thông báo thành công
            response.put("success", "Thông tin khách hàng đã được cập nhật!");
            return response;
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return response;
        } catch (Exception e) {
            response.put("error", "Có lỗi xảy ra. Vui lòng thử lại.");
            return response;
        }
    }


}
