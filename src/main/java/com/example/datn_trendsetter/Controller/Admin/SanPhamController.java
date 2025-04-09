package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SanPhamController {

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @Autowired
    private ChatLieuRepository chatLieuRepository;

    @Autowired
    private XuatXuRepository xuatSuRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private MauSacRepository mauSacRepository;

    @Autowired
    private KichThuocRepository kichThuocRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @RequestMapping("admin/san-pham")
    public String SanPham(Model model) {
        List<SanPham> sanPham = sanPhamRepository.findByDeleted(false,Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("sanPham",sanPham);
        return "Admin/SanPham/hien-thi";
    }

    @RequestMapping("admin/add-san-pham")
    public String addSanPham(Model model) {
        List<SanPhamChiTiet> sanPhamChiTiet = sanPhamChiTietRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<SanPham> sanPham = sanPhamRepository.findByTrangThai("Đang Hoạt Động",Sort.by(Sort.Direction.DESC, "id"));
        List<ChatLieu>  chatLieu = chatLieuRepository.findByTrangThai("Đang Hoạt Động",Sort.by(Sort.Direction.DESC, "id"));
        List<ThuongHieu> thuongHieu = thuongHieuRepository.findByTrangThai("Đang Hoạt Động",Sort.by(Sort.Direction.DESC, "id"));
        List<XuatXu> xuatXu = xuatSuRepository.findByTrangThai("Đang Hoạt Động",Sort.by(Sort.Direction.DESC, "id"));
        List<DanhMuc> danhMuc = danhMucRepository.findByTrangThai("Đang Hoạt Động",Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("sanPham",sanPham);
        model.addAttribute("sanPhamChiTiet",sanPhamChiTiet);
        model.addAttribute("thuongHieu",thuongHieu);
        model.addAttribute("xuatXu",xuatXu);
        model.addAttribute("chatLieu",chatLieu);
        model.addAttribute("danhMuc",danhMuc
        );
        return "Admin/SanPham/add-san-pham";
    }

    @RequestMapping("admin/detail-san-pham")
    public String detailSanPham(@RequestParam(value = "sanPhamId", required = false, defaultValue = "0") Integer sanPhamId,
                                Model model) {
        // Lấy sản phẩm theo ID
        Optional<SanPham> sanPhamOpt = sanPhamRepository.findById(sanPhamId);

        // Kiểm tra nếu sản phẩm không tồn tại
        if (sanPhamId == 0 || sanPhamOpt.isEmpty()) {
            return "redirect:/admin/san-pham?error=notfound";
        }

        SanPham sanPham = sanPhamOpt.get();

        // Lấy danh sách sản phẩm chi tiết theo ID sản phẩm
        List<SanPhamChiTiet> sanPhamChiTiet = sanPhamChiTietRepository.findBySanPhamId(sanPhamId);

        Map<MauSac, List<SanPhamChiTiet>> sanPhamChiTietGroupedByMauSac = sanPhamChiTiet.stream()
                .collect(Collectors.groupingBy(SanPhamChiTiet::getMauSac));

        model.addAttribute("sanPhamChiTietGroupedByMauSac", sanPhamChiTietGroupedByMauSac);

        // Lọc danh sách màu sắc và kích thước có trong sản phẩm chi tiết
        List<MauSac> mauSacSanPham = sanPhamChiTiet.stream()
                .map(SanPhamChiTiet::getMauSac)
                .distinct()
                .toList();

        List<KichThuoc> kichThuocSanPham = sanPhamChiTiet.stream()
                .map(SanPhamChiTiet::getKichThuoc)
                .distinct()
                .toList();

        // Lọc danh sách cặp Kích Thước - Màu Sắc đã tồn tại
        Set<String> kichThuocMauSacSet = sanPhamChiTiet.stream()
                .map(spct -> spct.getKichThuoc().getId() + "-" + spct.getMauSac().getId())
                .collect(Collectors.toSet());

        model.addAttribute("kichThuocMauSacSet", kichThuocMauSacSet);


        // Lấy danh sách các thuộc tính liên quan
        List<ChatLieu>  chatLieu = chatLieuRepository.findByTrangThai("Đang Hoạt Động",Sort.by(Sort.Direction.DESC, "id"));
        List<ThuongHieu> thuongHieu = thuongHieuRepository.findByTrangThai("Đang Hoạt Động",Sort.by(Sort.Direction.DESC, "id"));
        List<XuatXu> xuatXu = xuatSuRepository.findByTrangThai("Đang Hoạt Động",Sort.by(Sort.Direction.DESC, "id"));
        List<DanhMuc> danhMuc = danhMucRepository.findByTrangThai("Đang Hoạt Động",Sort.by(Sort.Direction.DESC, "id"));
        List<MauSac> mauSac = mauSacRepository.findByTrangThai("Đang Hoạt Động",Sort.by(Sort.Direction.DESC, "id"));
        List<KichThuoc> kichThuoc = kichThuocRepository.findByTrangThai("Đang Hoạt Động",Sort.by(Sort.Direction.DESC, "id"));

        // Đưa dữ liệu vào Model
        model.addAttribute("sanPham", sanPham);
        model.addAttribute("sanPhamChiTiet", sanPhamChiTiet);
        model.addAttribute("thuongHieu", thuongHieu);
        model.addAttribute("xuatXu", xuatXu);
        model.addAttribute("chatLieu", chatLieu);
        model.addAttribute("mauSac", mauSac);
        model.addAttribute("kichThuoc", kichThuoc);
        model.addAttribute("danhMuc", danhMuc);
        model.addAttribute("mauSacSanPham", mauSacSanPham);
        model.addAttribute("kichThuocSanPham", kichThuocSanPham);
        return "Admin/SanPham/detail-san-pham";
    }
}
