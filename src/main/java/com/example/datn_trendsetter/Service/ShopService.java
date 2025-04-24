package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.UserDetails;
import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Service
public class ShopService {
    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "NHANVIEN");
    private static final Map<Long, Lock> USER_LOCKS = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(30);

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
    private LichSuThanhToanRepository lichSuThanhToanRepository;

    @Autowired
    private PdfService pdfService;
    @Autowired
    private MauSacRepository mauSacRepository;

    @Autowired
    private KichThuocRepository kichThuocRepository;

    @Transactional
    public HoaDon createHoaDon(HoaDon hoaDon, Integer nhanVienId) throws Exception {
        if (hoaDon == null) {
            throw new Exception("Dữ liệu hóa đơn không được để trống.");
        }

        // Kiểm tra nhân viên
        Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(nhanVienId);
        if (nhanVienOpt.isEmpty()) {
            throw new Exception("Không tìm thấy thông tin nhân viên");
        }
        NhanVien nhanVien = nhanVienOpt.get();

        // Gán thông tin nhân viên vào hóa đơn
        hoaDon.setNhanVien(nhanVien);
        hoaDon.setNguoiTao(nhanVien.getHoTen());
        hoaDon.setNguoiSua(nhanVien.getHoTen());

        // Nếu khách hàng chưa có ID -> Lưu vào DB trước
        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getId() == null) {
            KhachHang khachHang = khachHangRepository.save(hoaDon.getKhachHang());
            hoaDon.setKhachHang(khachHang);
        }

        // Giới hạn số hóa đơn đang xử lý
        long countDangXuLy = hoaDonRepository.countByTrangThai("Đang xử lý");
        if (countDangXuLy >= 3) {
            throw new Exception("Đã đạt giới hạn 3 hóa đơn đang xử lý");
        }

        // Kiểm tra và tạo phương thức thanh toán mặc định nếu chưa có
        List<PhuongThucThanhToan> paymentMethods = createDefaultPaymentMethods(nhanVienId);

        // Gán phương thức thanh toán mặc định là "Tiền Mặt"
        if (!paymentMethods.isEmpty()) {
            hoaDon.setPhuongThucThanhToan(null); // Chọn phương thức đầu tiên
        }

        // Thiết lập thông tin hóa đơn
        hoaDon.setLoaiHoaDon("Tại Quầy");
        hoaDon.setTrangThai("Đang Xử Lý");
        hoaDon.setNgayTao(LocalDateTime.now());

        // Lưu hóa đơn
        hoaDon = hoaDonRepository.save(hoaDon);

        // Tạo mã hóa đơn duy nhất
        hoaDon.setMaHoaDon(generateUniqueMaHoaDon());

        // Lưu lịch sử hóa đơn
        saveLichSuHoaDon(hoaDon, nhanVienId);

        return hoaDonRepository.save(hoaDon);
    }

    private List<PhuongThucThanhToan> createDefaultPaymentMethods(Integer nhanVienId) throws Exception {
        List<String> existingMethods = phuongThucThanhToanRepository.findAll().stream()
                .map(PhuongThucThanhToan::getTenPhuongThuc)
                .toList();

        List<PhuongThucThanhToan> newMethods = new ArrayList<>();

        Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(nhanVienId);
        if (nhanVienOpt.isEmpty()) {
            throw new Exception("Không tìm thấy thông tin nhân viên");
        }
        NhanVien nhanVienSession = nhanVienOpt.get();

        if (!existingMethods.contains("Tiền Mặt")) {
            newMethods.add(new PhuongThucThanhToan("Tiền Mặt", "Thành Công", LocalDate.now(), LocalDate.now(),
                    nhanVienSession.getHoTen(), nhanVienSession.getHoTen(), false));
        }
        if (!existingMethods.contains("Chuyển Khoản")) {
            newMethods.add(new PhuongThucThanhToan("Chuyển Khoản", "Thành Công", LocalDate.now(), LocalDate.now(),
                    nhanVienSession.getHoTen(), nhanVienSession.getHoTen(), false));
        }

        if (!existingMethods.contains("VNPAY")) {
            newMethods.add(new PhuongThucThanhToan("VNPAY", "Thành Công", LocalDate.now(), LocalDate.now(),
                    nhanVienSession.getHoTen(), nhanVienSession.getHoTen(), false));
        }

        if (!newMethods.isEmpty()) {
            phuongThucThanhToanRepository.saveAll(newMethods);
        }

        // Lấy lại danh sách phương thức thanh toán để trả về
        return phuongThucThanhToanRepository.findAll();
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

    private void saveLichSuHoaDon(HoaDon hoaDon,Integer nhanVienId) throws Exception {

        Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(nhanVienId);
        if (nhanVienOpt.isEmpty()) {
            throw new Exception("Không tìm thấy thông tin nhân viên");
        }
        NhanVien nhanVienSession = nhanVienOpt.get();

        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setKhachHang(hoaDon.getKhachHang());
        lichSu.setNhanVien(nhanVienSession);
        lichSu.setHanhDong(hoaDon.getTrangThai());
        lichSu.setNgayTao(LocalDateTime.now());
        lichSu.setNgaySua(LocalDateTime.now());
        lichSu.setNguoiTao(nhanVienSession.getHoTen());
        lichSu.setNguoiSua(nhanVienSession.getHoTen());
        lichSu.setDeleted(false);
        lichSu.setGhiChu("Tạo mới hóa đơn, mã: " + hoaDon.getMaHoaDon());

        lichSuHoaDonRepository.save(lichSu);
    }

    @Transactional
    public void deleteHoaDon(Integer hoaDonId) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            if (hoaDon.getHoaDonChiTiet() != null && !hoaDon.getHoaDonChiTiet().isEmpty()) {
                for (HoaDonChiTiet hoaDonChiTiet : hoaDon.getHoaDonChiTiet()) {
                    if (hoaDonChiTiet == null) continue;

                    SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();
                    if (sanPhamChiTiet != null) {
                        Integer soLuongChiTiet = hoaDonChiTiet.getSoLuong();
                        if (soLuongChiTiet != null) {
                            sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() + soLuongChiTiet);
                            sanPhamChiTietRepository.save(sanPhamChiTiet);
                        }

                        SanPham sanPham = sanPhamChiTiet.getSanPham();
                        if (sanPham != null) {
                            updateStockForProduct(sanPham);
                        }
                    }
                }

                hoaDonChiTietRepository.deleteAll(hoaDon.getHoaDonChiTiet());
            }

            if (hoaDon.getLichSuHoaDon() != null && !hoaDon.getLichSuHoaDon().isEmpty()) {
                lichSuHoaDonRepository.deleteAll(hoaDon.getLichSuHoaDon());
            }

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

    // getAll
    public void getHoaDonAndProducts(Integer hoaDonId, Model model) {
        // Lấy danh sách hóa đơn có trạng thái "Đang Xử Lý"
        List<HoaDon> hoaDons = hoaDonRepository.findByTrangThai("Đang Xử Lý");

        // Đếm tổng số sản phẩm cho từng hóa đơn
        hoaDons.forEach(hoaDon -> {
            if (hoaDon.getHoaDonChiTiet() != null) {
                hoaDon.setTongSanPham(
                        hoaDon.getHoaDonChiTiet().stream()
                                .filter(Objects::nonNull) // Tránh null
                                .mapToInt(HoaDonChiTiet::getSoLuong)
                                .sum()
                );
            }
        });

        model.addAttribute("hoaDons", hoaDons);

        // Nếu không có hóa đơn ID, không cần xử lý tiếp
        if (hoaDonId == null) return;

        // Lấy hóa đơn từ DB
        Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(hoaDonId);
        if (hoaDonOpt.isEmpty()) return; // Nếu không tìm thấy hóa đơn, dừng lại

        HoaDon hoaDon = hoaDonOpt.get();
        model.addAttribute("hoaDon", hoaDon);

        // Lấy chi tiết hóa đơn
        List<HoaDonChiTiet> hoaDonChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        model.addAttribute("hoaDonChiTiet", hoaDonChiTiet);

        // Lấy danh sách sản phẩm có trạng thái "Còn Hàng"
        List<SanPhamChiTiet> allSanPhamChiTiet = sanPhamChiTietRepository.findByTrangThai("Còn Hàng");

        // Lọc sản phẩm theo hình ảnh duy nhất
        Map<String, SanPhamChiTiet> uniqueSanPhamMap = new LinkedHashMap<>();
        for (SanPhamChiTiet sp : allSanPhamChiTiet) {
            List<HinhAnh> hinhAnhs = sp.getHinhAnh();
            if (hinhAnhs != null && !hinhAnhs.isEmpty()) {
                String imageUrl = hinhAnhs.get(0).getUrlHinhAnh();
                uniqueSanPhamMap.putIfAbsent(imageUrl, sp); // Chỉ thêm nếu chưa có
            }
        }

        List<MauSac> mauSacs = mauSacRepository.findAll();
        model.addAttribute("danhSachMauSac", mauSacs);

        List<KichThuoc> kichThuocs = kichThuocRepository.findAll();
        model.addAttribute("danhSachKichThuoc", kichThuocs);

        // ✅ Lọc danh sách sản phẩm chi tiết Còn Hàng và có sản phẩm chính Đang Hoạt Động
        List<SanPhamChiTiet> filteredSanPhamChiTiet = uniqueSanPhamMap.values().stream()
                .filter(spct -> "Còn Hàng".equals(spct.getTrangThai())
                        && "Đang Hoạt Động".equals(spct.getSanPham().getTrangThai()))
                .collect(Collectors.toList());

        // ✅ Trộn ngẫu nhiên danh sách sản phẩm đã lọc
        Collections.shuffle(filteredSanPhamChiTiet);

        // ✅ Đưa danh sách vào model
        model.addAttribute("sanPhamChiTiet", filteredSanPhamChiTiet);


        // Lấy danh sách khách hàng và phương thức thanh toán
        Page<KhachHang> khachHangs = khachHangRepository.findAllByTrangThai("Đang Hoạt Động", Pageable.ofSize(5));
        List<PhuongThucThanhToan> listPhuongThucThanhToan = phuongThucThanhToanRepository.findAll();
        listPhuongThucThanhToan = listPhuongThucThanhToan.stream()
                .filter(p -> !"VNPAY".equals(p.getTenPhuongThuc()))
                .collect(Collectors.toList());

        model.addAttribute("khachHangs", khachHangs);
        model.addAttribute("listPhuongThucThanhToan", listPhuongThucThanhToan);

        // Tính tổng thành tiền của hóa đơn chi tiết
        Float tongThanhTien = hoaDonChiTietRepository.getTongThanhTienByHoaDonId(hoaDonId);

        // Lọc danh sách phiếu giảm giá nếu tổng tiền hợp lệ
        List<PhieuGiamGia> validPhieuGiamGia = (tongThanhTien != null)
                ? phieuGiamGiaRepository.findByDieuKienLessThanEqualAndTrangThaiAndDeletedFalse(tongThanhTien, "Đang Hoạt Động")
                : Collections.emptyList();

        model.addAttribute("listPhieuGiamGia", validPhieuGiamGia);
    }

    //     Thêm khách hàng vào hóa đơn
    public String addCustomerToInvoice(Integer hoaDonId, KhachHang khachHang) {
        // Kiểm tra đầu vào hợp lệ
        if (hoaDonId == null || khachHang == null || khachHang.getId() == null) {
            throw new IllegalArgumentException("ID hóa đơn và khách hàng không được null");
        }

        // Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Hóa đơn không tồn tại!"));

        // Kiểm tra nếu hóa đơn đã có khách hàng
        if (hoaDon.getKhachHang() != null) {
            throw new IllegalStateException("Hóa đơn này đã có khách hàng, không thể thêm mới!");
        }

        // Kiểm tra thông tin khách hàng có đầy đủ không
        if (khachHang.getHoTen() == null || khachHang.getSoDienThoai() == null || khachHang.getEmail() == null) {
            throw new IllegalArgumentException("Thông tin khách hàng không đầy đủ!");
        }

        // Cập nhật thông tin khách hàng vào hóa đơn
        hoaDon.setKhachHang(khachHang);
        hoaDon.setNguoiNhan(khachHang.getHoTen());
        hoaDon.setSoDienThoai(khachHang.getSoDienThoai());
        hoaDon.setEmail(khachHang.getEmail());

        // Tìm địa chỉ mặc định của khách hàng (không ảnh hưởng đến phí ship)
        DiaChi diaChi = diaChiRepository.findByKhachHangAndTrangThai(khachHang, "Mặc Định");

        // Cập nhật địa chỉ nhưng KHÔNG cập nhật phí ship
        if (diaChi != null) {
            hoaDon.setDiaChiCuThe(diaChi.getDiaChiCuThe());
            hoaDon.setPhuong(diaChi.getPhuong());
            hoaDon.setHuyen(diaChi.getHuyen());
            hoaDon.setThanhPho(diaChi.getThanhPho());
        }

        // Luôn đặt phí ship về 0
        hoaDon.setPhiShip(0F);

        // Tính tổng tiền hóa đơn
        float tongTienSanPham = hoaDon.getHoaDonChiTiet().stream()
                .map(HoaDonChiTiet::getThanhTien)
                .filter(Objects::nonNull)
                .reduce(0F, Float::sum);

        float giaTriGiamGia = (hoaDon.getPhieuGiamGia() != null && hoaDon.getPhieuGiamGia().getGiaTriGiam() != null)
                ? hoaDon.getPhieuGiamGia().getGiaTriGiam() : 0F;

        hoaDon.setTongTien(Math.max(tongTienSanPham - giaTriGiamGia, 0));

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
            hoaDon.setDiaChiCuThe(null);
            hoaDon.setPhuong(null);
            hoaDon.setHuyen(null);
            hoaDon.setThanhPho(null);
            hoaDon.setPhiShip(null); // Đặt phí ship về 0
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
            hoaDon.setDiaChiCuThe(null);
            hoaDon.setPhuong(null);
            hoaDon.setHuyen(null);
            hoaDon.setThanhPho(null);
            hoaDon.setPhiShip(null);

            // Set null cho khách hàng
            hoaDon.setKhachHang(null);
        }

        // Tính tổng tiền hóa đơn
        float tongTienSanPham = hoaDon.getHoaDonChiTiet().stream()
                .map(HoaDonChiTiet::getThanhTien)
                .filter(Objects::nonNull)
                .reduce(0F, Float::sum);

        float giaTriGiamGia = (hoaDon.getPhieuGiamGia() != null && hoaDon.getPhieuGiamGia().getGiaTriGiam() != null)
                ? hoaDon.getPhieuGiamGia().getGiaTriGiam() : 0F;

        float phiShip = Objects.requireNonNullElse(hoaDon.getPhiShip(), 0F);

        // Áp dụng công thức: tổng thành tiền trong hóa đơn chi tiết - phí ship - giá trị phiếu giảm giá
        hoaDon.setTongTien(Math.max(tongTienSanPham - phiShip - giaTriGiamGia, 0));

        // Lưu hóa đơn vào cơ sở dữ liệu
        hoaDonRepository.save(hoaDon);

        response.put("message", "Đã xóa khách hàng khỏi hóa đơn!");
        return response;
    }

    @Transactional
    public String updateShippingAddress(Integer hoaDonId, String nguoiNhan, String soDienThoai,
                                        String diaChiCuThe, String phuong, String huyen, String thanhPho, String ghiChu) {
        if (hoaDonId == null) {
            throw new IllegalArgumentException("ID hóa đơn không được null!");
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new EntityNotFoundException("Hóa đơn không tồn tại!"));

        if ("Đã Hoàn Thành".equalsIgnoreCase(hoaDon.getTrangThai()) ||
                "Đang Vận Chuyển".equalsIgnoreCase(hoaDon.getTrangThai())) {
            throw new IllegalStateException("Không thể cập nhật địa chỉ khi hóa đơn đã thanh toán hoặc đang vận chuyển!");
        }

        // Xóa địa chỉ cũ
        hoaDon.setNguoiNhan(null);
        hoaDon.setSoDienThoai(null);
        hoaDon.setDiaChiCuThe(null);
        hoaDon.setPhuong(null);
        hoaDon.setHuyen(null);
        hoaDon.setThanhPho(null);
        hoaDon.setGhiChu(null);

        // Cập nhật địa chỉ mới
        hoaDon.setNguoiNhan(nguoiNhan);
        hoaDon.setSoDienThoai(soDienThoai);
        hoaDon.setDiaChiCuThe(diaChiCuThe);
        hoaDon.setPhuong(phuong);
        hoaDon.setHuyen(huyen);
        hoaDon.setThanhPho(thanhPho);
        hoaDon.setLoaiHoaDon("Giao Hàng");
        hoaDon.setGhiChu(ghiChu);

        hoaDon.setPhiShip(30000F);

        // Tính tổng tiền hóa đơn
        float tongTienSanPham = hoaDon.getHoaDonChiTiet().stream()
                .map(HoaDonChiTiet::getThanhTien)
                .filter(Objects::nonNull)
                .reduce(0F, Float::sum);

        float giaTriGiamGia = (hoaDon.getPhieuGiamGia() != null && hoaDon.getPhieuGiamGia().getGiaTriGiam() != null)
                ? hoaDon.getPhieuGiamGia().getGiaTriGiam() : 0F;

        hoaDon.setTongTien(Math.max(tongTienSanPham + hoaDon.getPhiShip() - giaTriGiamGia, 0));

        // Lưu hóa đơn vào cơ sở dữ liệu
        hoaDonRepository.save(hoaDon);

        return "Địa chỉ được cập nhật thành công!";
    }


    public ResponseEntity<Map<String, String>> handleProductOrder(String action, Integer sanPhamChiTietId, Integer hoaDonId, Integer soLuong,
                                                                  Integer hoaDonChiTietId) {
        Map<String, String> response = new HashMap<>();
        switch (action) {
            case "add":
                return hoaDonChiTietService.addProductDetailToHoaDon(sanPhamChiTietId, hoaDonId, soLuong);
            case "update":
                return hoaDonChiTietService.updateQuantityOrder(hoaDonChiTietId, soLuong, hoaDonId);
            case "delete":
                return hoaDonChiTietService.deleteProductOrder(hoaDonChiTietId, hoaDonId);
            default:
                response.put("successMessage", "Hành động không hợp lệ");
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
    public String applyPhieuGiamGia(Integer hoaDonId, String tenPhieuGiamGia) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        if ("Đã Hoàn Thành".equalsIgnoreCase(hoaDon.getTrangThai())) {
            throw new RuntimeException("Không thể thay đổi phiếu giảm giá sau khi thanh toán.");
        }

        // Nếu không chọn phiếu giảm giá, xóa phiếu giảm giá hiện tại
        if (tenPhieuGiamGia == null || tenPhieuGiamGia.trim().isEmpty()) {
            hoaDon.setPhieuGiamGia(null);
            float phiShip = (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : 0F;
            float tongTienMoi = hoaDon.getHoaDonChiTiet().stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(0F, Float::sum) + phiShip;
            hoaDon.setTongTien(Math.max(tongTienMoi, 0));
            hoaDonRepository.save(hoaDon);
            return "Phiếu giảm giá đã được gỡ bỏ!";
        }

        // Tìm phiếu giảm giá mới
        PhieuGiamGia phieuGiamGiaMoi = phieuGiamGiaRepository.findByTenPhieuGiamGia(tenPhieuGiamGia)
                .orElseThrow(() -> new RuntimeException("Phiếu giảm giá không hợp lệ"));

        LocalDate now = LocalDate.now();
        if (phieuGiamGiaMoi.getNgayBatDau().isAfter(now)) {
            throw new RuntimeException("Phiếu giảm giá chưa đến thời gian áp dụng.");
        }
        if (phieuGiamGiaMoi.getNgayKetThuc().isBefore(now)) {
            throw new RuntimeException("Phiếu giảm giá đã hết hạn.");
        }

        // Kiểm tra tổng tiền sản phẩm
        float tongTienSanPham = hoaDon.getHoaDonChiTiet().stream()
                .map(HoaDonChiTiet::getThanhTien)
                .filter(Objects::nonNull)
                .reduce(0F, Float::sum);
        if (tongTienSanPham < phieuGiamGiaMoi.getDieuKien()) {
            throw new RuntimeException("Điều kiện phiếu giảm giá không thỏa mãn.");
        }

        // Gán phiếu giảm giá mới cho hóa đơn
        hoaDon.setPhieuGiamGia(phieuGiamGiaMoi);
        float phiShip = (hoaDon.getPhiShip() != null) ? hoaDon.getPhiShip() : 0F;
        float tongTienMoi = Math.max(tongTienSanPham + phiShip - phieuGiamGiaMoi.getGiaTriGiam(), 0);
        hoaDon.setTongTien(tongTienMoi);
        hoaDonRepository.save(hoaDon);

        return "Phiếu giảm giá đã được áp dụng!";
    }

    @Transactional
    public String confirmPayment(Integer hoaDonId, RedirectAttributes redirectAttributes) {
        try {
            // Tìm hóa đơn theo ID
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // Kiểm tra nếu hóa đơn không có sản phẩm
            if (hoaDon.getHoaDonChiTiet() == null || hoaDon.getHoaDonChiTiet().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chưa có sản phẩm!");
                return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
            }

            // Kiểm tra nếu chưa chọn phương thức thanh toán
            if (hoaDon.getPhuongThucThanhToan() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chưa chọn phương thức thanh toán!");
                return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
            }

            // ✅ Tính tổng tiền sản phẩm (bỏ qua sản phẩm có giá trị null)
            Float tongTien = hoaDon.getHoaDonChiTiet().stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(0F, Float::sum);

            // ✅ Cộng thêm phí ship (nếu có)
            tongTien += Objects.requireNonNullElse(hoaDon.getPhiShip(), 0F);

            // ✅ Trừ đi giá trị phiếu giảm giá nếu có
            if (hoaDon.getPhieuGiamGia() != null) {
                PhieuGiamGia phieuGiamGia = hoaDon.getPhieuGiamGia();

                // Kiểm tra nếu phiếu giảm giá đã bị ngừng hoạt động
                if ("Ngừng Hoạt Động".equals(phieuGiamGia.getTrangThai())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Phiếu giảm giá này đã ngừng hoạt động!");
                    return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
                }

                if (phieuGiamGia.getSoLuotSuDung() > 0) {
                    Float giaTriGiam = Objects.requireNonNullElse(phieuGiamGia.getGiaTriGiam(), 0F);
                    tongTien = Math.max(tongTien - giaTriGiam, 0F); // Đảm bảo không âm

                    // Giảm số lượt sử dụng của phiếu giảm giá
                    phieuGiamGia.setSoLuotSuDung(phieuGiamGia.getSoLuotSuDung() - 1);
                    if (phieuGiamGia.getSoLuotSuDung() == 0) {
                        phieuGiamGia.setTrangThai("Ngừng Hoạt Động");
                    }
                    phieuGiamGiaRepository.save(phieuGiamGia);
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Phiếu giảm giá này đã hết lượt sử dụng!");
                    return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
                }
            }

            // ✅ Cập nhật tổng tiền vào hóa đơn
            hoaDon.setTongTien(tongTien);
            hoaDon.setNgaySua(LocalDateTime.now());

            // Kiểm tra nếu loại hóa đơn là "Tại Quầy"
            if ("Tại Quầy".equals(hoaDon.getLoaiHoaDon())) {
                // ✅ Lưu lịch sử thanh toán
                saveLichSuThanhToan(hoaDon, tongTien);

                // ✅ Lưu lịch sử hóa đơn
                saveLichSuHoaDon(hoaDon, "Đã Hoàn Thành");

                hoaDon.setLoaiGiaoDich("Trả Trước");
                hoaDon.setTrangThai("Đã Hoàn Thành");
                redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thành công!");

                // ✅ Lưu hóa đơn vào database
                hoaDonRepository.save(hoaDon);

                return "redirect:/admin/sell-counter"; // Redirect về trang bán hàng tại quầy
            }


            // ✅ Xử lý theo loại hóa đơn Giao Hàng
            if ("Giao Hàng".equals(hoaDon.getLoaiHoaDon())) {
                // Kiểm tra nếu địa chỉ giao hàng bị thiếu
                if (hoaDon.getDiaChiCuThe() == null || hoaDon.getDiaChiCuThe().isBlank() ||
                        hoaDon.getHuyen() == null || hoaDon.getHuyen().isBlank() ||
                        hoaDon.getPhuong() == null || hoaDon.getPhuong().isBlank() ||
                        hoaDon.getThanhPho() == null || hoaDon.getThanhPho().isBlank()) {

                    redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng cập nhật địa chỉ giao hàng!");
                    return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
                }

                // Kiểm tra nếu phí ship chưa được cập nhật
                if (hoaDon.getPhiShip() == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Hãy cập nhật phí ship trước khi xác nhận thanh toán!");
                    return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
                }

                hoaDon.setThoiGianNhanDuKien(LocalDate.now().plusDays(3));

                if ("Trả Trước".equals(hoaDon.getLoaiGiaoDich())) {
                    hoaDon.setTrangThai("Đã Xác Nhận");
                    saveLichSuThanhToan(hoaDon, tongTien);
                    redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thành công! Đơn hàng đang chờ xác nhận.");
                } else if ("Trả Sau".equals(hoaDon.getLoaiGiaoDich())) {
                    hoaDon.setTrangThai("Đã Xác Nhận");
//                    saveLichSuThanhToan(hoaDon, 0.0f);
                    redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng sẽ được thanh toán sau, đang chờ xác nhận!");
                } else {
                    hoaDon.setTrangThai("Đã Xác Nhận");
                    redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đang chờ giao hàng!");
                }

                // ✅ Lưu lịch sử hóa đơn
                saveLichSuHoaDon(hoaDon, hoaDon.getTrangThai());
            }

            // ✅ Lưu hóa đơn vào database
            hoaDonRepository.save(hoaDon);

            return "redirect:/admin/order-details?hoaDonId=" + hoaDonId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        }
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

    private void saveLichSuHoaDon(HoaDon hoaDon, String hanhDong) {

        LichSuHoaDon lichSuHoaDon = new LichSuHoaDon();
        lichSuHoaDon.setHoaDon(hoaDon);
        lichSuHoaDon.setKhachHang(hoaDon.getKhachHang());
        lichSuHoaDon.setNhanVien(hoaDon.getNhanVien());
        lichSuHoaDon.setHanhDong(hanhDong);
        lichSuHoaDon.setNgayTao(LocalDateTime.now());
        lichSuHoaDon.setNguoiTao(hoaDon.getNguoiTao());
        lichSuHoaDon.setDeleted(false);
        lichSuHoaDon.setGhiChu("Hóa đơn: " + hoaDon.getMaHoaDon() + " - " + hanhDong);
        lichSuHoaDonRepository.save(lichSuHoaDon);
    }


    public Map<String, Object> addNewCustomer(Integer hoaDonId, String nguoiNhan, String soDienThoai) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (nguoiNhan == null || nguoiNhan.trim().isEmpty() || soDienThoai == null || soDienThoai.trim().isEmpty()) {
                response.put("status", "warning");
                response.put("message", "Thông tin người nhận không hợp lệ!");
                return response;
            }

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            if (hoaDon.getKhachHang() != null) {
                response.put("status", "warning");
                response.put("message", "Hóa đơn này đã có thông tin khách hàng. Vui lòng kiểm tra lại.");
                return response;
            }

            // Cập nhật thông tin người nhận
            hoaDon.setNguoiNhan(nguoiNhan);
            hoaDon.setSoDienThoai(soDienThoai);
            hoaDon.setDiaChiCuThe(null);
            hoaDon.setPhuong(null);
            hoaDon.setHuyen(null);
            hoaDon.setThanhPho(null);
            hoaDon.setKhachHang(null);
            hoaDon.setPhiShip(0.0F);

            hoaDonRepository.save(hoaDon);

            response.put("status", "success");
            response.put("message", "Thông tin người nhận đã được cập nhật!");
            return response;

        } catch (RuntimeException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Có lỗi xảy ra. Vui lòng thử lại.");
            return response;
        }
    }


}