package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Entity.HoaDonChiTiet;
import com.example.datn_trendsetter.Entity.SanPham;
import com.example.datn_trendsetter.Entity.SanPhamChiTiet;
import com.example.datn_trendsetter.Repository.HoaDonChiTietRepository;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Repository.SanPhamChiTietRepository;
import com.example.datn_trendsetter.Repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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


    // Phương thức cập nhật số lượng trong hóa đơn chi tiết
    public String updateQuantityOrder(Integer hoaDonChiTietId, Integer soLuong, Integer hoaDonId, RedirectAttributes redirectAttributes) {
        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(hoaDonChiTietId).orElse(null);
        if (hoaDonChiTiet == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn chi tiết không tồn tại!");
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        }

        // Kiểm tra số lượng mới
        if (soLuong < 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số lượng phải lớn hơn 0!");
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        }

        // Lấy sản phẩm chi tiết
        SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();

        // Kiểm tra số lượng tồn kho
        int soLuongTonKho = sanPhamChiTiet.getSoLuong();
        int soLuongBanHang = hoaDonChiTiet.getSoLuong();
        int soLuongMoi = soLuong - soLuongBanHang;

        if (soLuongTonKho < soLuongMoi) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số lượng tồn kho không đủ.");
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        }

        // Cập nhật thông tin hóa đơn chi tiết
        hoaDonChiTiet.setSoLuong(soLuong);
        Float thanhTien = sanPhamChiTiet.getGia().floatValue() * soLuong;
        hoaDonChiTiet.setThanhTien(thanhTien);
        hoaDonChiTietRepository.save(hoaDonChiTiet);

        // Cập nhật tồn kho sản phẩm chi tiết
        sanPhamChiTiet.setSoLuong(soLuongTonKho - soLuongMoi);

        // Kiểm tra số lượng còn lại và cập nhật trạng thái
        if (sanPhamChiTiet.getSoLuong() == 0) {
            sanPhamChiTiet.setTrangThai("Hết Hàng");
        } else {
            sanPhamChiTiet.setTrangThai("Còn Hàng");
        }

        sanPhamChiTietRepository.save(sanPhamChiTiet);

        // Cập nhật tồn kho sản phẩm chính
        SanPham sanPham = sanPhamChiTiet.getSanPham();
        updateStockForProduct(sanPham);

        // Cập nhật tổng tiền hóa đơn
        updateInvoiceTotal(hoaDonId);

        return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
    }



    // Phương thức thêm chi tiết sản phẩm vào hóa đơn
    public String addProductDetailToHoaDon(Integer sanPhamChiTietId, Integer hoaDonId, Integer soLuong, RedirectAttributes redirectAttributes) {
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(sanPhamChiTietId).orElse(null);
        if (sanPhamChiTiet == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm chi tiết không tồn tại.");
            return "redirect:/admin/sell-counter";
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại.");
            return "redirect:/admin/sell-counter";
        }

        // Kiểm tra số lượng tồn kho
        if (sanPhamChiTiet.getSoLuong() < soLuong) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số lượng tồn kho không đủ.");
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
        }

        // Tạo mới hoặc cập nhật sản phẩm chi tiết trong hóa đơn
        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findByHoaDonIdAndSanPhamChiTietId(hoaDonId, sanPhamChiTietId).orElse(new HoaDonChiTiet());
        hoaDonChiTiet.setHoaDon(hoaDon);
        hoaDonChiTiet.setSanPhamChiTiet(sanPhamChiTiet);
        hoaDonChiTiet.setSoLuong(hoaDonChiTiet.getSoLuong() != null ? hoaDonChiTiet.getSoLuong() + soLuong : soLuong);
        hoaDonChiTiet.setGia(sanPhamChiTiet.getGia().floatValue());
        hoaDonChiTiet.setThanhTien(hoaDonChiTiet.getSoLuong() * hoaDonChiTiet.getGia());
        hoaDonChiTietRepository.save(hoaDonChiTiet);

        // Trừ số lượng tồn kho
        sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() - soLuong);

        // Kiểm tra số lượng còn lại và cập nhật trạng thái sản phẩm chi tiết
        if (sanPhamChiTiet.getSoLuong() == 0) {
            sanPhamChiTiet.setTrangThai("Hết Hàng");
        } else {
            sanPhamChiTiet.setTrangThai("Còn Hàng");
        }

        sanPhamChiTietRepository.save(sanPhamChiTiet);

        // Cập nhật số lượng tồn kho cho sản phẩm chính
        SanPham sanPham = sanPhamChiTiet.getSanPham();
        updateStockForProduct(sanPham);

        // Cập nhật tổng tiền hóa đơn
        updateInvoiceTotal(hoaDonId);

        redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm vào hóa đơn thành công!");
        return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
    }

    // Phương thức xóa sản phẩm khỏi hóa đơn
    public String deleteProductOrder(Integer hoaDonChiTietId, Integer hoaDonId, RedirectAttributes redirectAttributes) {
        HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(hoaDonChiTietId).orElse(null);
        if (hoaDonChiTiet == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn chi tiết không tồn tại.");
            return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
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

        // Xóa hóa đơn chi tiết
        hoaDonChiTietRepository.deleteById(hoaDonChiTietId);

        // Cập nhật tổng tiền hóa đơn
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hóa đơn không tồn tại.");
            return "redirect:/admin/sell-counter";
        }

        updateInvoiceTotal(hoaDonId);

        // Kiểm tra tổng tiền và điều kiện phiếu giảm giá
        if (hoaDon.getPhieuGiamGia() != null) {
            Double tongTien = hoaDon.getTongTien();
            Float dieuKienPhieuGiamGia = hoaDon.getPhieuGiamGia().getDieuKien();
            if (tongTien < dieuKienPhieuGiamGia) {
                // Gỡ bỏ phiếu giảm giá khỏi hóa đơn
                hoaDon.setPhieuGiamGia(null);
                hoaDonRepository.save(hoaDon);
                redirectAttributes.addFlashAttribute("warningMessage", "Tổng tiền nhỏ hơn điều kiện áp dụng phiếu giảm giá. Phiếu giảm giá đã bị gỡ bỏ.");
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Xóa hóa đơn chi tiết thành công!");
        return "redirect:/admin/sell-counter?hoaDonId=" + hoaDonId;
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
            double tongTien = hoaDonChiTietRepository.findByHoaDonId(hoaDonId).stream()
                    .mapToDouble(HoaDonChiTiet::getThanhTien)
                    .sum();
            hoaDon.setTongTien(tongTien);
            hoaDonRepository.save(hoaDon);
        }
    }
}
