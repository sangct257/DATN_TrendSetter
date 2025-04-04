package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.SanPham;
import com.example.datn_trendsetter.Repository.SanPhamRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDate;
import java.util.List;
import com.example.datn_trendsetter.DTO.ProductDetailDTO;
import com.example.datn_trendsetter.DTO.SanPhamChiTietDTO;
import com.example.datn_trendsetter.DTO.SanPhamDTO;
import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import com.example.datn_trendsetter.Service.HinhAnhService;
import com.example.datn_trendsetter.Service.SanPhamService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/san-pham")
public class SanPhamAPIController {

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private MauSacRepository mauSacRepository;

    @Autowired
    private KichThuocRepository kichThuocRepository;

    @Autowired
    private HinhAnhService hinhAnhService;

    @Autowired
    private HinhAnhRepository hinhAnhRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;
    private ResponseEntity<Map<String, Object>> response(String message, boolean success) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<SanPham>> getSanPhamByTrangThai(@RequestParam(required = false) String trangThai) {
        List<SanPham> sanPhamList;
        if (trangThai != null && !trangThai.isEmpty()) {
            sanPhamList = sanPhamRepository.findByTrangThai(trangThai, Sort.by(Sort.Direction.DESC, "id"));
        } else {
            sanPhamList = sanPhamRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }
        return ResponseEntity.ok(sanPhamList);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countSanPhamByTrangThai() {
        long hoatDong = sanPhamRepository.countByTrangThai("Đang Hoạt Động");
        long ngungHoatDong = sanPhamRepository.countByTrangThai("Ngừng Hoạt Động");
        long tong = sanPhamRepository.count();

        Map<String, Long> countMap = Map.of(
                "Đang Hoạt Động", hoatDong,
                "Ngừng Hoạt Động", ngungHoatDong,
                "Tất Cả", tong
        );

        return ResponseEntity.ok(countMap);
    }

    // API để xóa mềm sản phẩm (khi nhấn vào trạng thái)
    @PutMapping("/toggle-status/{id}")
    public ResponseEntity<?> toggleSanPhamStatus(@PathVariable Integer id) {
        boolean updated = sanPhamService.toggleSanPhamStatus(id);
        if (updated) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Cập nhật trạng thái thành công!"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Sản phẩm không tồn tại!"));
        }
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addSanPham(@RequestBody SanPhamDTO sanPhamDTO, HttpSession session) {
        return sanPhamService.addSanPham(sanPhamDTO,session);
    }

    @PutMapping("/update-san-pham/{id}")
    public ResponseEntity<?> updateSanPham(@PathVariable Integer id, @RequestBody SanPhamDTO sanPhamDTO) {
        try {
            return ResponseEntity.ok(sanPhamService.updateSanPham(id, sanPhamDTO));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", e.getReason(),
                    "status", e.getStatusCode().value()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Lỗi hệ thống! Vui lòng thử lại sau.",
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
            ));
        }
    }

    @PostMapping("/add-mau-sac-kich-thuoc")
    public ResponseEntity<String> addMauSacKichThuoc(@RequestBody ProductDetailDTO request ,HttpSession session) throws Exception {
        Optional<SanPham> sanPhamOpt = sanPhamRepository.findById(request.getSanPhamId());
        if (!sanPhamOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Sản phẩm không tồn tại");
        }
        SanPham sanPham = sanPhamOpt.get();

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
        }

        for (Integer mauSacId : request.getMauSacIds()) {
            for (Integer kichThuocId : request.getKichThuocIds()) {
                boolean exists = sanPhamChiTietRepository.existsBySanPhamAndMauSacIdAndKichThuocId(
                        sanPham, mauSacId, kichThuocId);
                if (!exists) {
                    SanPhamChiTiet chiTiet = new SanPhamChiTiet();
                    chiTiet.setSanPham(sanPham);
                    chiTiet.setSoLuong(1);
                    chiTiet.setGia(100000F);
                    chiTiet.setTrangThai(chiTiet.getSoLuong() > 0 ? "Còn Hàng" : "Hết Hàng");
                    chiTiet.setMauSac(mauSacRepository.findById(mauSacId).orElse(null));
                    chiTiet.setKichThuoc(kichThuocRepository.findById(kichThuocId).orElse(null));
                    chiTiet.setNguoiTao(nhanVienSession.getHoTen());
                    chiTiet.setNguoiSua(nhanVienSession.getHoTen());
                    chiTiet.setNgayTao(LocalDate.now());
                    chiTiet.setNgaySua(LocalDate.now());
                    chiTiet.setDeleted(false);
                    sanPhamChiTietRepository.save(chiTiet);
                    capNhatSoLuongTonKhoSanPham(chiTiet.getSanPham());
                }
            }
        }
        return ResponseEntity.ok("Thêm thành công!");
    }



    @PutMapping("/update-product-details")
    public ResponseEntity<?> updateProductDetails(@RequestBody List<SanPhamChiTietDTO> request, HttpSession session) throws Exception {
        Map<Integer, SanPhamChiTietDTO> idProductAndProduct = request.stream().collect(Collectors.toMap(SanPhamChiTietDTO::getId, Function.identity()));
        List<Integer> idProductDetails = request.stream().map(SanPhamChiTietDTO::getId).toList();

        List<SanPhamChiTiet> sanPhamChiTiets = sanPhamChiTietRepository.findAllByIdIn(idProductDetails);
        if (sanPhamChiTiets.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vui lòng chọn sản phẩm chi tiết");
        }

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
        }

        // Kiểm tra số lượng trước khi cập nhật
        for (SanPhamChiTietDTO sanPhamChiTietDTO : request) {
            if (sanPhamChiTietDTO.getSoLuong() < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số lượng sản phẩm chi tiết không thể nhỏ hơn 0.");
            }
        }

        // Cập nhật sản phẩm chi tiết
        sanPhamChiTiets.forEach(sanPhamChiTiet -> {
            SanPhamChiTietDTO sanPhamChiTietDTO = idProductAndProduct.getOrDefault(sanPhamChiTiet.getId(), null);
            if (ObjectUtils.isNotEmpty(sanPhamChiTietDTO)) {
                sanPhamChiTiet.setSoLuong(sanPhamChiTietDTO.getSoLuong());
                sanPhamChiTiet.setGia(sanPhamChiTietDTO.getGia().floatValue());
                sanPhamChiTiet.setNguoiSua(nhanVienSession.getHoTen());
                sanPhamChiTiet.setTrangThai(sanPhamChiTietDTO.getSoLuong() > 0 ? "Còn Hàng" : "Hết Hàng");
                sanPhamChiTiet.setNgaySua(LocalDate.now());
            }
        });

        sanPhamChiTietRepository.saveAll(sanPhamChiTiets);
        capNhatSoLuongTonKhoSanPham(sanPhamChiTiets.get(0).getSanPham());
        return response("Cập nhật chi tiết sản phẩm thành công!", true);
    }

    @PutMapping("/update")
    public ResponseEntity<?> capNhatChiTietSanPham(@RequestBody SanPhamChiTietDTO request, HttpSession session) throws Exception {
        // Kiểm tra số lượng không được nhỏ hơn 0
        if (request.getSoLuong() < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số lượng sản phẩm không thể nhỏ hơn 0.");
        }

        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(request.getId()).orElse(null);
        if (sanPhamChiTiet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chi tiết sản phẩm không tồn tại!");
        }

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
        }

        // Cập nhật thông tin chi tiết sản phẩm
        sanPhamChiTiet.setSoLuong(request.getSoLuong());
        sanPhamChiTiet.setGia(request.getGia().floatValue());
        sanPhamChiTiet.setNguoiSua(nhanVienSession.getHoTen());
        sanPhamChiTiet.setTrangThai(request.getSoLuong() > 0 ? "Còn Hàng" : "Hết Hàng");
        sanPhamChiTiet.setNgaySua(LocalDate.now());
        sanPhamChiTietRepository.save(sanPhamChiTiet);

        // Cập nhật tồn kho sản phẩm chính
        capNhatSoLuongTonKhoSanPham(sanPhamChiTiet.getSanPham());
        return ResponseEntity.ok("Cập nhật chi tiết sản phẩm thành công!");
    }

    @Transactional
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSPCT(@PathVariable("id") Integer id) {
        // Kiểm tra xem sản phẩm chi tiết có tồn tại không
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(id).orElse(null);
        if (sanPhamChiTiet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chi tiết sản phẩm không tồn tại!");
        }

        // Kiểm tra xem sản phẩm chi tiết có trong hóa đơn chi tiết không
        boolean existsInHoaDonChiTiet = hoaDonChiTietRepository.existsBySanPhamChiTiet(sanPhamChiTiet);
        if (existsInHoaDonChiTiet) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không thể xóa sản phẩm đang được thống kê!");
        }

        // Xóa hình ảnh liên quan đến sản phẩm chi tiết trước
        hinhAnhRepository.deleteBySanPhamChiTiet(sanPhamChiTiet);

        // Xóa sản phẩm chi tiết
        sanPhamChiTietRepository.deleteById(id);

        // Cập nhật số lượng tồn kho cho sản phẩm
        capNhatSoLuongTonKhoSanPham(sanPhamChiTiet.getSanPham());

        return ResponseEntity.ok("Xóa sản phẩm chi tiết thành công!");
    }

    private void capNhatSoLuongTonKhoSanPham(SanPham sanPham) {
        List<SanPhamChiTiet> listSanPhamChiTiet = sanPhamChiTietRepository.findBySanPham(sanPham);
        int tongSoLuong = listSanPhamChiTiet.stream()
                .filter(spct -> spct.getSoLuong() != null)
                .mapToInt(SanPhamChiTiet::getSoLuong)
                .sum();

        sanPham.setSoLuong(tongSoLuong);
        sanPham.setTrangThai(tongSoLuong > 0 ? "Đang Hoạt Động" : "Ngừng Hoạt Động");
        sanPhamRepository.save(sanPham);
    }

    @PostMapping("/upload/{sanPhamChiTietId}")
    public ResponseEntity<?> uploadImages(@PathVariable Integer sanPhamChiTietId, @RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body("Không có tệp nào được tải lên!");
            }

            SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(sanPhamChiTietId).orElse(null);
            if (sanPhamChiTiet == null) {
                return ResponseEntity.badRequest().body("Sản phẩm không tồn tại!");
            }

            List<HinhAnh> hinhAnhs = hinhAnhService.uploadImages(files, sanPhamChiTiet);
            List<String> savedImageUrls = hinhAnhs.stream().map(HinhAnh::getUrlHinhAnh).toList();

            return ResponseEntity.ok(savedImageUrls);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi upload: " + e.getMessage());
        }
    }

    @DeleteMapping("/xoa/{hinhAnhId}")
    public ResponseEntity<String> xoaHinhAnh(@PathVariable Integer hinhAnhId) {
        Optional<HinhAnh> optionalHinhAnh = hinhAnhRepository.findById(hinhAnhId);
        if (optionalHinhAnh.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hình ảnh không tồn tại!");
        }

        HinhAnh hinhAnh = optionalHinhAnh.get();
        String publicId = hinhAnh.getPublicId();

        boolean xoaThanhCong = hinhAnhService.xoaAnhTrenCloudinary(publicId);
        if (!xoaThanhCong) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi xóa ảnh trên Cloudinary!");
        }

        hinhAnhRepository.delete(hinhAnh);
        return ResponseEntity.ok("Xóa hình ảnh thành công!");
    }



}
