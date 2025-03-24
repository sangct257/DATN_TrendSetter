package com.example.datn_trendsetter.Service;

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
    private LichSuThanhToanRepository lichSuThanhToanRepository;

    @Autowired
    private PdfService pdfService;
    @Autowired
    private MauSacRepository mauSacRepository;

    @Autowired
    private KichThuocRepository kichThuocRepository;

    @Transactional
    public HoaDon createHoaDon(HoaDon hoaDon, HttpSession session) throws Exception {
        if (hoaDon == null) {
            throw new Exception("Dữ liệu hóa đơn không được để trống.");
        }

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("user");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập để tạo hóa đơn.");
        }

        // Gán nhân viên từ session vào hóa đơn
        hoaDon.setNhanVien(nhanVienSession);

        if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getId() == null) {
            KhachHang khachHang = khachHangRepository.save(hoaDon.getKhachHang());
            hoaDon.setKhachHang(khachHang);
        }

        // Kiểm tra và tạo phương thức thanh toán nếu chưa tồn tại
        createDefaultPaymentMethods(session);

        // Kiểm tra số lượng hóa đơn đang xử lý
        long countDangXuLy = hoaDonRepository.countByTrangThai("Đang xử lý");
        if (countDangXuLy >= 3) {
            throw new Exception("Đã đạt giới hạn 3 hóa đơn đang xử lý. Không thể tạo thêm.");
        }

        // Lấy phương thức thanh toán đầu tiên (nếu có)
        Optional<PhuongThucThanhToan> optionalPaymentMethod = phuongThucThanhToanRepository.findFirstByOrderByIdAsc();
        optionalPaymentMethod.ifPresent(hoaDon::setPhuongThucThanhToan);

        // Thiết lập thông tin hóa đơn
        hoaDon.setTongTien(null);
        hoaDon.setPhiShip(null);
        hoaDon.setLoaiHoaDon("Tại Quầy");
        hoaDon.setTrangThai("Đang Xử Lý");
        hoaDon.setNguoiTao(hoaDon.getNhanVien().getHoTen());
        hoaDon.setNguoiSua(hoaDon.getNhanVien().getHoTen());
        hoaDon.setNgayTao(LocalDateTime.now());

        // Lưu hóa đơn lần đầu để lấy ID
        hoaDon = hoaDonRepository.save(hoaDon);

        // Tạo mã hóa đơn ngẫu nhiên và cập nhật lại hóa đơn
        hoaDon.setMaHoaDon(generateUniqueMaHoaDon());
        hoaDonRepository.save(hoaDon);

        // Lưu lịch sử hóa đơn
        saveLichSuHoaDon(hoaDon,session);

        return hoaDon;
    }



    private void createDefaultPaymentMethods(HttpSession session) throws Exception {

        List<String> existingMethods = phuongThucThanhToanRepository.findAll().stream()
                .map(PhuongThucThanhToan::getTenPhuongThuc)
                .toList();

        List<PhuongThucThanhToan> newMethods = new ArrayList<>();

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("user");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập để tạo hóa đơn.");
        }

        if (!existingMethods.contains("Tiền Mặt")) {
            newMethods.add(new PhuongThucThanhToan("Tiền Mặt", "Thành Công", LocalDate.now(), LocalDate.now(), nhanVienSession.getHoTen(), nhanVienSession.getHoTen(), false));
        }
        if (!existingMethods.contains("Chuyển Khoản")) {
            newMethods.add(new PhuongThucThanhToan("Chuyển Khoản", "Thành Công", LocalDate.now(), LocalDate.now(), nhanVienSession.getHoTen(), nhanVienSession.getHoTen(), false));
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

    private void saveLichSuHoaDon(HoaDon hoaDon,HttpSession session) throws Exception {

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("user");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập để tạo hóa đơn.");
        }

        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setKhachHang(hoaDon.getKhachHang());
        lichSu.setNhanVien(hoaDon.getNhanVien());
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
            // Tìm hóa đơn theo ID
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // Kiểm tra và khôi phục số lượng sản phẩm trong chi tiết hóa đơn
            if (hoaDon.getHoaDonChiTiet() != null && !hoaDon.getHoaDonChiTiet().isEmpty()) {
                for (HoaDonChiTiet hoaDonChiTiet : hoaDon.getHoaDonChiTiet()) {
                    if (hoaDonChiTiet == null) continue; // Bỏ qua nếu null

                    // Lấy sản phẩm chi tiết và khôi phục số lượng
                    SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();
                    if (sanPhamChiTiet != null) {
                        Integer soLuongChiTiet = hoaDonChiTiet.getSoLuong();
                        if (soLuongChiTiet != null) {
                            sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() + soLuongChiTiet);
                            sanPhamChiTietRepository.save(sanPhamChiTiet);
                        }

                        // Cập nhật số lượng sản phẩm chính
                        SanPham sanPham = sanPhamChiTiet.getSanPham();
                        if (sanPham != null) {
                            updateStockForProduct(sanPham);
                        }
                    }
                }

                // Xóa chi tiết hóa đơn sau khi khôi phục số lượng sản phẩm
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

        // Lấy danh sách sản phẩm duy nhất và trộn ngẫu nhiên
        List<SanPhamChiTiet> uniqueSanPhamChiTiet = new ArrayList<>(uniqueSanPhamMap.values());
        Collections.shuffle(uniqueSanPhamChiTiet);
        model.addAttribute("sanPhamChiTiet", uniqueSanPhamChiTiet);

        // Lấy danh sách khách hàng và phương thức thanh toán
        Page<KhachHang> khachHangs = khachHangRepository.findAllByTrangThai("Đang Hoạt Động", Pageable.ofSize(5));
        List<PhuongThucThanhToan> listPhuongThucThanhToan = phuongThucThanhToanRepository.findAll();

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
    public String addCustomerToInvoice(Integer hoaDonId, Integer khachHangId) {
        // Kiểm tra đầu vào hợp lệ
        if (hoaDonId == null || khachHangId == null) {
            throw new IllegalArgumentException("ID hóa đơn và ID khách hàng không được null");
        }

        // Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Hóa đơn không tồn tại!"));

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

        if ("Giao Hàng".equalsIgnoreCase(phieuGiamGiaMoi.getLoaiApDung()) &&
                !"Giao Hàng".equalsIgnoreCase(hoaDon.getLoaiHoaDon())) {
            throw new RuntimeException("Phiếu giảm giá chỉ áp dụng cho đơn hàng giao hàng.");
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
                redirectAttributes.addFlashAttribute("errorMessage", "Chưa có sản phẩm sao thanh toán má!");
                return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
            }

            // Kiểm tra nếu chưa chọn phương thức thanh toán
            if (hoaDon.getPhuongThucThanhToan() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chưa chọn phương thức thanh toán sao thanh toán má!");
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
                    hoaDon.setTrangThai("Chờ Xác Nhận");
                    saveLichSuThanhToan(hoaDon, tongTien);
                    redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thành công! Đơn hàng đang chờ xác nhận.");
                } else if ("Trả Sau".equals(hoaDon.getLoaiGiaoDich())) {
                    hoaDon.setTrangThai("Chờ Xác Nhận");
                    saveLichSuThanhToan(hoaDon, 0.0f);
                    redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng sẽ được thanh toán sau, đang chờ xác nhận!");
                } else {
                    hoaDon.setTrangThai("Chờ Xác Nhận");
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
        LichSuThanhToan lichSuThanhToan = new LichSuThanhToan();
        lichSuThanhToan.setHoaDon(hoaDon);
        lichSuThanhToan.setNhanVien(hoaDon.getNhanVien());
        lichSuThanhToan.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        lichSuThanhToan.setSoTienThanhToan(soTien);
        lichSuThanhToan.setThoiGianThanhToan(LocalDateTime.now());
        lichSuThanhToan.setTrangThai("Đã Thanh Toán");
        lichSuThanhToan.setGhiChu("Thanh toán hóa đơn " + hoaDon.getMaHoaDon());

        // Check if the payment method is "Tiền Mặt"
        if (!"Tiền Mặt".equals(hoaDon.getPhuongThucThanhToan().getTenPhuongThuc())) {
            // If not Tiền Mặt, generate a transaction code
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
            hoaDon.setDiaChiCuThe(null);
            hoaDon.setPhuong(null);
            hoaDon.setHuyen(null);
            hoaDon.setThanhPho(null);
            hoaDon.setKhachHang(null);

            // Kiểm tra nếu không có khách hàng thì set phí ship về 0
            if (hoaDon.getKhachHang() == null) {
                hoaDon.setPhiShip(0.0F);
            } else {
                hoaDon.setPhiShip(hoaDon.getPhiShip()); // Giữ nguyên phí ship nếu có khách hàng
            }

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
