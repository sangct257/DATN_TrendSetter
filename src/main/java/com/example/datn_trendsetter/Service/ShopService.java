package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.ShippingUpdateRequest;
import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private PdfService pdfService;

    // Tạo hóa đơn
    public String createHoaDon(HoaDon hoaDon, RedirectAttributes redirectAttributes) {
        long countDangXuLy = hoaDonRepository.countByTrangThai("Đang xử lý");
        if (countDangXuLy >= 3) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã đạt giới hạn 3 hóa đơn đang xử lý. Không thể tạo thêm.");
            return "redirect:/admin/sell-counter"; // Redirect về danh sách hóa đơn
        }

        // Lấy phương thức thanh toán đầu tiên từ cơ sở dữ liệu
        PhuongThucThanhToan firstPaymentMethod = phuongThucThanhToanRepository.findFirstByOrderByIdAsc();

        if (firstPaymentMethod != null) {
            // Gán phương thức thanh toán đầu tiên cho hóa đơn
            hoaDon.setPhuongThucThanhToan(firstPaymentMethod);
        } else {
            // Nếu không có phương thức thanh toán, bạn có thể gán một giá trị mặc định hoặc thông báo lỗi
            redirectAttributes.addFlashAttribute("errorMessage", "Không có phương thức thanh toán hợp lệ.");
            return "redirect:/admin/sell-counter"; // Redirect về danh sách hóa đơn
        }

        hoaDon.setPhiShip(0.0);
        hoaDon.setIsGiaoHang(false);
        hoaDon.setTrangThai("Đang Xử Lý");
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDonRepository.save(hoaDon);
        return "redirect:/admin/sell-counter";
    }


    @Transactional
    public void deleteHoaDon(Integer hoaDonId) {
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

    public void getHoaDonAndProducts(Integer hoaDonId, Model model, RedirectAttributes redirectAttributes) {
        // Lấy danh sách hóa đơn, sản phẩm, khách hàng
        List<HoaDon> hoaDons = hoaDonRepository.findByTrangThai("Đang Xử Lý");
        List<SanPhamChiTiet> sanPhamChiTiet = sanPhamChiTietRepository.findByTrangThai("Còn Hàng");
        List<KhachHang> khachHangs = khachHangRepository.findAll();
        List<PhuongThucThanhToan> listPhuongThucThanhToan = phuongThucThanhToanRepository.findAll();
        model.addAttribute("hoaDons", hoaDons);
        model.addAttribute("sanPhamChiTiet", sanPhamChiTiet);
        model.addAttribute("khachHangs", khachHangs);
        model.addAttribute("listPhuongThucThanhToan", listPhuongThucThanhToan);

        // Đếm sản phẩm cho từng hóa đơn
        for (HoaDon hoaDon : hoaDons) {
            int tongSanPham = hoaDon.getHoaDonChiTiet()
                    .stream()
                    .mapToInt(HoaDonChiTiet::getSoLuong)
                    .sum();
            hoaDon.setTongSanPham(tongSanPham);
        }

        // Kiểm tra nếu hoaDonId không phải null
        if (hoaDonId != null) {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
            if (hoaDon == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại");
                return;
            }

            // Lấy danh sách chi tiết hóa đơn
            List<HoaDonChiTiet> hoaDonChiTiet = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            if (hoaDonChiTiet == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chi tiết hóa đơn không tồn tại");
                return;
            }

            // Lọc danh sách phiếu giảm giá dựa trên tổng tiền
            Double tongTien = hoaDon.getTongTien();
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
            model.addAttribute("isGiaoHang", hoaDon.getIsGiaoHang());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "ID hóa đơn không hợp lệ");
        }
    }

    //     Thêm khách hàng vào hóa đơn
    public String addCustomerToInvoice(Integer hoaDonId, Integer khachHangId, RedirectAttributes redirectAttributes) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại!");
            return "redirect:/admin/sell-counter";
        }

        KhachHang khachHang = khachHangRepository.findById(khachHangId).orElse(null);
        if (khachHang == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khách hàng không tồn tại!");
            return "redirect:/admin/sell-counter";
        }

        // Tìm địa chỉ mặc định của khách hàng (trangThai = true/1)
        DiaChi diaChi = diaChiRepository.findByKhachHangAndTrangThai(khachHang, true);

        // Nếu không tìm thấy địa chỉ mặc định, gán các trường địa chỉ là null
        if (diaChi == null) {
            hoaDon.setSoNha(null);
            hoaDon.setPhuong(null);
            hoaDon.setHuyen(null);
            hoaDon.setThanhPho(null);
        } else {
            // Cập nhật thông tin địa chỉ vào hóa đơn nếu tìm thấy địa chỉ mặc định
            hoaDon.setSoNha(diaChi.getSoNha());
            hoaDon.setPhuong(diaChi.getPhuong());
            hoaDon.setHuyen(diaChi.getHuyen());
            hoaDon.setThanhPho(diaChi.getThanhPho());
        }

        // Kiểm tra nếu thành phố là Hà Nội, gán phí ship = 30k
        if ("Hà Nội".equalsIgnoreCase(hoaDon.getThanhPho())) {
            hoaDon.setPhiShip(30000.0); // Gán phí ship là 30k
        } else {
            hoaDon.setPhiShip(100000.0); // Hoặc phí ship là 0 nếu không phải Hà Nội
        }

        // Cập nhật khách hàng
        hoaDon.setKhachHang(khachHang);
        hoaDon.setIsGiaoHang(true);

        // Lưu hóa đơn
        hoaDonRepository.save(hoaDon);

        redirectAttributes.addFlashAttribute("successMessage", "Thông tin giao hàng đã được cập nhật!");
        return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
    }

    //     Thêm khách hàng vào hóa đơn
    public String deleteCustomerToInvoice(Integer hoaDonId, RedirectAttributes redirectAttributes) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);

        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại!");
            return "redirect:/admin/sell-counter";
        }

        // Cập nhật thông tin giao hàng
        hoaDon.setSoNha(null);
        hoaDon.setPhuong(null);
        hoaDon.setHuyen(null);
        hoaDon.setThanhPho(null);
        hoaDon.setKhachHang(null);
        hoaDon.setPhiShip(0.0);
        hoaDon.setIsGiaoHang(false);

        // Lưu hóa đơn
        hoaDonRepository.save(hoaDon);

        redirectAttributes.addFlashAttribute("successMessage", "Thông tin giao hàng đã được cập nhật!");
        return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
    }


    // Phương thức xử lý các hành động thêm, sửa, xóa sản phẩm trong hóa đơn
    public String handleProductOrder(String action, Integer idSanPhamChiTiet, Integer idHoaDon, Integer soLuong,
                                     Integer idHoaDonChiTiet, RedirectAttributes redirectAttributes) {
        switch (action) {
            case "add":
                return hoaDonChiTietService.addProductDetailToHoaDon(idSanPhamChiTiet, idHoaDon, soLuong, redirectAttributes);
            case "update":
                return hoaDonChiTietService.updateQuantityOrder(idHoaDonChiTiet, soLuong, idHoaDon, redirectAttributes);
            case "delete":
                return hoaDonChiTietService.deleteProductOrder(idHoaDonChiTiet, idHoaDon, redirectAttributes);
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

    public String updateShippingAddress(ShippingUpdateRequest request, RedirectAttributes redirectAttributes) {
        try {
            // Lấy hóa đơn từ cơ sở dữ liệu
            HoaDon hoaDon = hoaDonRepository.findById(request.getHoaDonId())
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // Kiểm tra khách hàng khớp với hóa đơn
            if (!hoaDon.getKhachHang().getId().equals(request.getKhachHangId())) {
                throw new RuntimeException("Khách hàng không khớp với hóa đơn!");
            }

            // Kiểm tra trạng thái giao hàng và các thông tin địa chỉ (phường, huyện, thành phố, số nhà)
            if (hoaDon.getIsGiaoHang() != null && hoaDon.getIsGiaoHang() &&
                    (request.getSoNha() == null || request.getPhuong() == null || request.getHuyen() == null || request.getThanhPho() == null)) {
                throw new RuntimeException("Vui lòng nhập đầy đủ thông tin địa chỉ giao hàng!");
            }

            // Cập nhật địa chỉ vào hóa đơn
            hoaDon.setSoNha(request.getSoNha());
            hoaDon.setPhuong(request.getPhuong());
            hoaDon.setHuyen(request.getHuyen());
            hoaDon.setThanhPho(request.getThanhPho());


            // Kiểm tra nếu thành phố là Hà Nội, gán phí ship = 30k
            if ("Hà Nội".equalsIgnoreCase(hoaDon.getThanhPho())) {
                hoaDon.setPhiShip(30000.0); // Gán phí ship là 30k
            } else {
                hoaDon.setPhiShip(100000.0); // Hoặc phí ship là 0 nếu không phải Hà Nội
            }

            // Lưu lại hóa đơn đã cập nhật
            hoaDonRepository.save(hoaDon);

            // Lưu thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật địa chỉ giao hàng thành công!");

            return "redirect:/admin/sell-counter?hoaDonId=" + request.getHoaDonId();

        } catch (Exception e) {
            // Xử lý lỗi
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật địa chỉ thất bại: " + e.getMessage());
            return "redirect:/admin/sell-counter?hoaDonId=" + request.getHoaDonId();
        }
    }




    public String confirmPayment(Integer hoaDonId, RedirectAttributes redirectAttributes) {
        try {
            // Lấy hóa đơn từ cơ sở dữ liệu
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // Kiểm tra nếu hóa đơn chi tiết không tồn tại
            if (hoaDon.getHoaDonChiTiet() == null || hoaDon.getHoaDonChiTiet().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chưa có sản phâm sao thanh toán má!");
                return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
            }

            // Kiểm tra nếu phương thức thanh toán chưa được chọn
            if (hoaDon.getPhuongThucThanhToan() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Chưa chọn phuong thực thanh toán sao thanh toán má!");
                return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
            }

            // Kiểm tra điều kiện nếu khách hàng không null
            if (hoaDon.getKhachHang() != null) {
                // Kiểm tra thông tin địa chỉ nếu khách hàng tồn tại
                if (hoaDon.getPhuong() == null || hoaDon.getPhuong().isEmpty() ||
                        hoaDon.getHuyen() == null || hoaDon.getHuyen().isEmpty() ||
                        hoaDon.getThanhPho() == null || hoaDon.getThanhPho().isEmpty() ||
                        hoaDon.getSoNha() == null || hoaDon.getSoNha() == 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Chưa điền thông tin giao hàng sao giao hàng");
                    return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
                }
                // Cập nhật trạng thái đang vận chuyển nếu có khách
                hoaDon.setTongTien(hoaDon.getTongTien()-hoaDon.getPhieuGiamGia().getGiaTri()+hoaDon.getPhiShip());
                hoaDon.setTrangThai("Đặt Hàng");
            } else {
                // Cập nhật trạng thái thanh toán thành công nếu không có khách hàng
                hoaDon.setTongTien(hoaDon.getTongTien()-hoaDon.getPhieuGiamGia().getGiaTri()+hoaDon.getPhiShip());
                hoaDon.setTrangThai("Thành Công");
            }

            // Kiểm tra nếu có phiếu giảm giá áp dụng
            if (hoaDon.getPhieuGiamGia() != null) {
                // Cập nhật tổng tiền sau khi áp dụng phiếu giảm giá
                Double tongTienMoi = hoaDon.getTongTien() - hoaDon.getPhieuGiamGia().getGiaTri();
                hoaDon.setTongTien(tongTienMoi);
            }

            // Cập nhật thời gian sửa đổi hóa đơn
            hoaDon.setNgaySua(LocalDateTime.now());

            // Lưu lại hóa đơn đã cập nhật vào cơ sở dữ liệu
            hoaDonRepository.save(hoaDon);

            // Tạo file PDF
            File pdfFile = pdfService.generateInvoicePdf(hoaDon);

            // Lưu thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thành công! Hóa đơn PDF đã được tạo tại: " + pdfFile.getAbsolutePath());

            return "redirect:/admin/sell-counter";
        } catch (Exception e) {
            // Xử lý lỗi nếu có
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        }
    }





}
