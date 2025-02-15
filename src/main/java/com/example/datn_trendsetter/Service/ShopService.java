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
    private PdfService pdfService;

    @Autowired
    private GHNService ghnService;

    public ResponseEntity<?> createHoaDon(HoaDon hoaDon) {
        // Kiểm tra xem có ít nhất 2 phương thức thanh toán (tiền mặt và chuyển khoản) chưa
        PhuongThucThanhToan tienMat = phuongThucThanhToanRepository.findByTenPhuongThuc("Tiền mặt");
        PhuongThucThanhToan chuyenKhoan = phuongThucThanhToanRepository.findByTenPhuongThuc("Chuyển khoản");

        if (tienMat == null) {
            // Nếu chưa có phương thức thanh toán Tiền mặt, tạo mới
            PhuongThucThanhToan newTienMat = new PhuongThucThanhToan();
            newTienMat.setTenPhuongThuc("Tiền Mặt");
            newTienMat.setTrangThai("Thành công");
            newTienMat.setNgayTao(LocalDate.now());
            newTienMat.setNguoiTao(hoaDon.getNguoiTao());
            newTienMat.setDeleted(false);
            phuongThucThanhToanRepository.save(newTienMat);
        }

        if (chuyenKhoan == null) {
            // Nếu chưa có phương thức thanh toán Chuyển khoản, tạo mới
            PhuongThucThanhToan newChuyenKhoan = new PhuongThucThanhToan();
            newChuyenKhoan.setTenPhuongThuc("Chuyển Khoản");
            newChuyenKhoan.setTrangThai("Thành Công");
            newChuyenKhoan.setNgayTao(LocalDate.now());
            newChuyenKhoan.setNguoiTao(hoaDon.getNguoiTao());
            newChuyenKhoan.setDeleted(false);
            phuongThucThanhToanRepository.save(newChuyenKhoan);
        }

        // Kiểm tra số lượng hóa đơn đang xử lý
        long countDangXuLy = hoaDonRepository.countByTrangThai("Đang xử lý");
        if (countDangXuLy >= 3) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Đã đạt giới hạn 3 hóa đơn đang xử lý. Không thể tạo thêm.");
        }

        // Lấy phương thức thanh toán đầu tiên (phương thức đã tạo)
        PhuongThucThanhToan firstPaymentMethod = phuongThucThanhToanRepository.findFirstByOrderByIdAsc();
        if (firstPaymentMethod == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không có phương thức thanh toán hợp lệ.");
        }
        hoaDon.setPhuongThucThanhToan(firstPaymentMethod);

        hoaDon.setTongTien(0.0F);
        hoaDon.setPhiShip(0.0F);
        hoaDon.setLoaiHoaDon(null);
        hoaDon.setTrangThai("Đang Xử Lý");
        hoaDon.setNgayTao(LocalDateTime.now());

        // Lưu hóa đơn lần đầu để có ID tự động
        hoaDon = hoaDonRepository.save(hoaDon);

        // Tạo mã hóa đơn ngẫu nhiên với 6 chữ số
        String maHoaDon;
        Random random = new Random();

        do {
            int randomNumber = 100000 + random.nextInt(900000); // Số ngẫu nhiên từ 100000 - 999999
            maHoaDon = "MH" + randomNumber;
        } while (hoaDonRepository.existsByMaHoaDon(maHoaDon));

        // Gán mã hóa đơn
        hoaDon.setMaHoaDon(maHoaDon);

        // Lưu lại để cập nhật mã hóa đơn
        hoaDonRepository.save(hoaDon);

        // Tạo đối tượng LichSuHoaDon để lưu lịch sử
        LichSuHoaDon lichSuHoaDon = new LichSuHoaDon();
        lichSuHoaDon.setHoaDon(hoaDon); // Liên kết với hóa đơn mới
        lichSuHoaDon.setKhachHang(hoaDon.getKhachHang());
        lichSuHoaDon.setNhanVien(hoaDon.getNhanVien());
        lichSuHoaDon.setHanhDong(hoaDon.getTrangThai());
        lichSuHoaDon.setNgayTao(LocalDateTime.now());
        lichSuHoaDon.setNguoiTao(hoaDon.getNguoiTao());
        lichSuHoaDon.setDeleted(false); // Đánh dấu là không xóa
        lichSuHoaDon.setGhiChu("Tạo mới hóa đơn, mã: " + hoaDon.getMaHoaDon());

        // Lưu lịch sử hóa đơn
        lichSuHoaDonRepository.save(lichSuHoaDon);
        return ResponseEntity.ok(hoaDon); // Trả về hóa đơn vừa tạo
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


            //Lấy tất cả sản phẩm có trạng thái "Còn Hàng"
            List<SanPhamChiTiet> allSanPhamChiTiet = sanPhamChiTietRepository.findByTrangThai("Còn Hàng");

            // Sử dụng HashSet để đảm bảo không có sản phẩm bị trùng
            Set<SanPhamChiTiet> uniqueSanPhamChiTiet = new HashSet<>(allSanPhamChiTiet);

            // Trộn danh sách sản phẩm ngẫu nhiên
            List<SanPhamChiTiet> shuffledSanPhamChiTiet = new ArrayList<>(uniqueSanPhamChiTiet);
            Collections.shuffle(shuffledSanPhamChiTiet);

            // Chỉ lấy 5 sản phẩm ngẫu nhiên
            List<SanPhamChiTiet> sanPhamChiTiet = shuffledSanPhamChiTiet.stream().limit(5).collect(Collectors.toList());

            Page<KhachHang> khachHangs = khachHangRepository.findAllByTrangThai("Đang Hoạt Động", Pageable.ofSize(5));
            List<PhuongThucThanhToan> listPhuongThucThanhToan = phuongThucThanhToanRepository.findAll();

            model.addAttribute("sanPhamChiTiet", sanPhamChiTiet);
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


    public void getProducts(Integer hoaDonId, Model model) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDonId != null) {
            //Lấy tất cả sản phẩm có trạng thái "Còn Hàng"
            List<SanPhamChiTiet> allSanPhamChiTiet = sanPhamChiTietRepository.findByTrangThai("Còn Hàng");

            // Sử dụng HashSet để đảm bảo không có sản phẩm bị trùng
            Set<SanPhamChiTiet> uniqueSanPhamChiTiet = new HashSet<>(allSanPhamChiTiet);

            // Trộn danh sách sản phẩm ngẫu nhiên
            List<SanPhamChiTiet> shuffledSanPhamChiTiet = new ArrayList<>(uniqueSanPhamChiTiet);
            Collections.shuffle(shuffledSanPhamChiTiet);

            // Chỉ lấy 5 sản phẩm ngẫu nhiên
            List<SanPhamChiTiet> sanPhamChiTiet = shuffledSanPhamChiTiet.stream().limit(5).collect(Collectors.toList());

            model.addAttribute("sanPhamChiTiet", sanPhamChiTiet);
            model.addAttribute("hoaDon", hoaDon);
        }
    }


    //     Thêm khách hàng vào hóa đơn
    public String addCustomerToInvoice(Integer hoaDonId, Integer khachHangId, RedirectAttributes redirectAttributes) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại!");
            return "redirect:/admin/sell-counter";
        }

        // Kiểm tra nếu hóa đơn đã có khách hàng thì không cho phép thay đổi
        if (hoaDon.getNguoiNhan() != null && !hoaDon.getNguoiNhan().trim().isEmpty() &&
                hoaDon.getSoDienThoai() != null && !hoaDon.getSoDienThoai().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn đã có khách hàng, vui lòng xóa !");
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        }


        KhachHang khachHang = khachHangRepository.findById(khachHangId).orElse(null);
        if (khachHang == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khách hàng không tồn tại!");
            return "redirect:/admin/sell-counter";
        }

        // Thêm họ tên và số điện thoại khách hàng vào hóa đơn
        hoaDon.setNguoiNhan(khachHang.getHoTen());
        hoaDon.setSoDienThoai(khachHang.getSoDienThoai());

        // Tìm địa chỉ mặc định của khách hàng
        DiaChi diaChi = diaChiRepository.findByKhachHangAndTrangThai(khachHang, "Mặc Định");

        if (diaChi == null) {
            hoaDon.setSoNha(null);
            hoaDon.setPhuong(null);
            hoaDon.setHuyen(null);
            hoaDon.setThanhPho(null);
        } else {
            hoaDon.setSoNha(diaChi.getSoNha());
            hoaDon.setPhuong(diaChi.getPhuong());
            hoaDon.setHuyen(diaChi.getHuyen());
            hoaDon.setThanhPho(diaChi.getThanhPho());
        }

        // Xác định phí ship
        if (hoaDon.getThanhPho() == null) {
            hoaDon.setPhiShip(0.0F);
        } else if ("Hà Nội".equalsIgnoreCase(hoaDon.getThanhPho())) {
            hoaDon.setPhiShip(30000.0F);
        } else {
            hoaDon.setPhiShip(100000.0F);
        }

        // Cập nhật khách hàng vào hóa đơn
        hoaDon.setKhachHang(khachHang);
        hoaDon.setLoaiHoaDon("Giao Hàng");

        // Lưu hóa đơn
        hoaDonRepository.save(hoaDon);

        redirectAttributes.addFlashAttribute("successMessage", "Thông tin khách hàng đã được thêm vào hóa đơn!");
        return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
    }

    //     Xóa khách hàng vào hóa đơn
    public String deleteCustomerToInvoice(Integer hoaDonId, RedirectAttributes redirectAttributes) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);

        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại!");
            return "redirect:/admin/sell-counter";
        }

        // Thêm họ tên và số điện thoại khách hàng vào hóa đơn
        hoaDon.setNguoiNhan(null);
        hoaDon.setSoDienThoai(null);
        hoaDon.setSoNha(null);
        hoaDon.setPhuong(null);
        hoaDon.setHuyen(null);
        hoaDon.setThanhPho(null);
        hoaDon.setKhachHang(null);
        hoaDon.setPhiShip(0.0F);
        hoaDon.setLoaiHoaDon(null);
        hoaDon.setLoaiHoaDon("Tại Quầy");

        // Lưu hóa đơn
        hoaDonRepository.save(hoaDon);

        redirectAttributes.addFlashAttribute("successMessage", "Thông tin khách hàng đã được xoá khỏi hoá đơn!");
        return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
    }


    // Phương thức xử lý các hành động thêm, sửa, xóa sản phẩm trong hóa đơn
    public String handleProductOrder(String action, Integer sanPhamChiTietId, Integer hoaDonId, Integer soLuong,
                                     Integer hoaDonChiTietId, RedirectAttributes redirectAttributes) {
        switch (action) {
            case "add":
                return hoaDonChiTietService.addProductDetailToHoaDon(sanPhamChiTietId, hoaDonId, soLuong, redirectAttributes);
            case "update":
                return hoaDonChiTietService.updateQuantityOrder(hoaDonChiTietId, soLuong, hoaDonId, redirectAttributes);
            case "delete":
                return hoaDonChiTietService.deleteProductOrder(hoaDonChiTietId, hoaDonId, redirectAttributes);
            default:
                return "redirect:/admin/sell-counter"; // Action không hợp lệ
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

    public String applyPhieuGiamGia(Integer hoaDonId, String tenChuongTrinh, RedirectAttributes redirectAttributes) {
        try {
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

                // Lưu lại hóa đơn đã cập nhật vào cơ sở dữ liệu
                hoaDonRepository.save(hoaDon);

                // Thêm thông báo thành công
                redirectAttributes.addFlashAttribute("successMessage", "Phiếu giảm giá đã được áp dụng!");
            } else {
                // Nếu điều kiện phiếu giảm giá không thỏa mãn
                redirectAttributes.addFlashAttribute("errorMessage", "Điều kiện phiếu giảm giá không thỏa mãn.");
            }

            // Chuyển hướng về trang sell-counter với hoaDonId
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;

        } catch (Exception e) {
            // Bắt lỗi và thêm thông báo lỗi
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        }
    }

    public String updateShippingAddress(Integer hoaDonId, Integer soNha, String nguoiNhan ,String soDienThoai,  String phuong, String huyen, String thanhPho, RedirectAttributes redirectAttributes) {
        try {
            // Lấy hóa đơn từ cơ sở dữ liệu
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // Cập nhật địa chỉ vào hóa đơn
            hoaDon.setNguoiNhan(nguoiNhan);
            hoaDon.setSoDienThoai(soDienThoai);
            hoaDon.setSoNha(soNha);  // Gán số nhà
            hoaDon.setPhuong(phuong);  // Gán phường
            hoaDon.setHuyen(huyen);  // Gán huyện
            hoaDon.setThanhPho(thanhPho);  // Gán thành phố

            // Kiểm tra nếu thành phố là Hà Nội, gán phí ship = 30k
            if (hoaDon.getThanhPho() == null) {
                hoaDon.setPhiShip(0.0F); // Nếu thành phố là null thì gán phí ship là 0
            } else if ("Hà Nội".equalsIgnoreCase(hoaDon.getThanhPho())) {
                hoaDon.setPhiShip(30000.0F); // Gán phí ship là 30k nếu thành phố là Hà Nội
            } else {
                hoaDon.setPhiShip(100000.0F); // Hoặc phí ship là 100k nếu không phải Hà Nội
            }

            // Lưu lại hóa đơn sau khi cập nhật
            hoaDonRepository.save(hoaDon);

            // Thêm thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật địa chỉ giao hàng thành công!");

            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        } catch (Exception e) {
            // Xử lý lỗi
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật địa chỉ thất bại: " + e.getMessage());
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        }
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
            lichSuHoaDon.setGhiChu("Xác nhận hóa đơn, mã: " + hoaDon.getMaHoaDon());

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


    public String addNewCustomer(Integer hoaDonId, String nguoiNhan, String soDienThoai, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra nếu nguoiNhan và soDienThoai đều null thì không thực hiện
            if (nguoiNhan == null || nguoiNhan.trim().isEmpty() || soDienThoai == null || soDienThoai.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", " Đã có thông tin người nhận!");
                return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
            }

            // Lấy hóa đơn từ cơ sở dữ liệu
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // Cập nhật họ tên và số điện thoại vào hóa đơn
            hoaDon.setNguoiNhan(nguoiNhan);  // Gán họ tên vào hóa đơn
            hoaDon.setSoDienThoai(soDienThoai);  // Gán số điện thoại vào hóa đơn
            hoaDon.setLoaiHoaDon("Giao Hàng");
            hoaDon.setSoNha(null);
            hoaDon.setPhuong(null);
            hoaDon.setHuyen(null);
            hoaDon.setThanhPho(null);

            if (hoaDon.getThanhPho() == null) {
                hoaDon.setPhiShip(0.0F); // Nếu thành phố là null thì gán phí ship là 0
            } else if ("Hà Nội".equalsIgnoreCase(hoaDon.getThanhPho())) {
                hoaDon.setPhiShip(30000.0F); // Gán phí ship là 30k nếu thành phố là Hà Nội
            } else {
                hoaDon.setPhiShip(100000.0F); // Hoặc phí ship là 100k nếu không phải Hà Nội
            }

            // Lưu hóa đơn sau khi cập nhật
            hoaDonRepository.save(hoaDon);

            // Quay lại trang bán hàng với thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "Thông tin khách hàng đã được cập nhật!");
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        } catch (Exception e) {
            // Xử lý lỗi nếu có
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        }
    }


}
