package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LichSuHoaDonService {
    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    public List<LichSuHoaDon> getAll() {
        return lichSuHoaDonRepository.findAll();
    }

    public void getOrderDetails(Integer hoaDonId, Model model, Integer page) {
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
            List<LichSuHoaDon> listLichSuHoaDon = lichSuHoaDonRepository.findByHoaDon_Id(hoaDonId);
            // Lọc danh sách phiếu giảm giá dựa trên tổng tiền
            model.addAttribute("hoaDon", hoaDon);
            model.addAttribute("listLichSuHoaDon", listLichSuHoaDon);
            model.addAttribute("hoaDonChiTiet", hoaDonChiTiet);
            model.addAttribute("hoaDon", hoaDon);
        }
    }

}
