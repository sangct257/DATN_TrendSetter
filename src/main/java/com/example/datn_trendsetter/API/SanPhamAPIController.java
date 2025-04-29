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
        Map<String, String> response = new HashMap<>();

        try {
            String result = sanPhamService.toggleSanPhamStatus(id);

            switch (result) {
                case "SUCCESS":
                    response.put("status", "success");
                    response.put("message", "Cập nhật trạng thái sản phẩm thành công!");
                    return ResponseEntity.ok(response);

                case "OUT_OF_STOCK":
                    response.put("status", "warning");
                    response.put("message", "Sản phẩm hết hàng!");
                    return ResponseEntity.badRequest().body(response);

                case "NOT_FOUND":
                default:
                    response.put("status", "error");
                    response.put("message", "Không tìm thấy sản phẩm!");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
    public ResponseEntity<?> updateProductDetails(@RequestBody List<SanPhamChiTietDTO> request, HttpSession session) {
        Map<String, String> response = new HashMap<>();

        try {
            if (request == null || request.isEmpty()) {
                response.put("status", "warning");
                response.put("message", "Danh sách sản phẩm không được để trống.");
                return ResponseEntity.badRequest().body(response);
            }

            Map<Integer, SanPhamChiTietDTO> idProductAndProduct = request.stream()
                    .collect(Collectors.toMap(SanPhamChiTietDTO::getId, Function.identity()));
            List<Integer> idProductDetails = request.stream().map(SanPhamChiTietDTO::getId).toList();

            List<SanPhamChiTiet> sanPhamChiTiets = sanPhamChiTietRepository.findAllByIdIn(idProductDetails);
            if (sanPhamChiTiets.isEmpty()) {
                response.put("status", "warning");
                response.put("message", "Không tìm thấy sản phẩm chi tiết tương ứng.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để thực hiện thao tác này.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            for (SanPhamChiTietDTO dto : request) {
                if (dto.getSoLuong() < 0) {
                    response.put("status", "warning");
                    response.put("message", "Số lượng sản phẩm không thể nhỏ hơn 0.");
                    return ResponseEntity.badRequest().body(response);
                }
                if (dto.getGia() < 1000) {
                    response.put("status", "warning");
                    response.put("message", "Giá sản phẩm không thể nhỏ hơn 1000.");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            sanPhamChiTiets.forEach(spct -> {
                SanPhamChiTietDTO dto = idProductAndProduct.get(spct.getId());
                if (dto != null) {
                    spct.setSoLuong(dto.getSoLuong());
                    spct.setGia(dto.getGia().floatValue());
                    spct.setNguoiSua(nhanVienSession.getHoTen());
                    spct.setTrangThai(dto.getSoLuong() > 0 ? "Còn Hàng" : "Hết Hàng");
                    spct.setNgaySua(LocalDate.now());
                }
            });

            sanPhamChiTietRepository.saveAll(sanPhamChiTiets);
            capNhatSoLuongTonKhoSanPham(sanPhamChiTiets.get(0).getSanPham());

            response.put("status", "success");
            response.put("message", "Cập nhật chi tiết sản phẩm thành công!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PutMapping("/update")
    public ResponseEntity<?> capNhatChiTietSanPham(@RequestBody SanPhamChiTietDTO request, HttpSession session) {
        Map<String, String> response = new HashMap<>();

        try {
            if (request.getSoLuong() < 0) {
                response.put("status", "warning");
                response.put("message", "Số lượng sản phẩm không thể nhỏ hơn 0.");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getGia() < 1000) {
                response.put("status", "warning");
                response.put("message", "Giá sản phẩm không thể nhỏ hơn 1000.");
                return ResponseEntity.badRequest().body(response);
            }

            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(request.getId()).orElse(null);
            if (spct == null) {
                response.put("status", "warning");
                response.put("message", "Chi tiết sản phẩm không tồn tại!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            spct.setSoLuong(request.getSoLuong());
            spct.setGia(request.getGia().floatValue());
            spct.setNguoiSua(nhanVienSession.getHoTen());
            spct.setTrangThai(request.getSoLuong() > 0 ? "Còn Hàng" : "Hết Hàng");
            spct.setNgaySua(LocalDate.now());
            sanPhamChiTietRepository.save(spct);

            capNhatSoLuongTonKhoSanPham(spct.getSanPham());

            response.put("status", "success");
            response.put("message", "Cập nhật chi tiết sản phẩm thành công!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSPCT(@PathVariable("id") Integer id) {
        Map<String, String> response = new HashMap<>();

        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(id).orElse(null);
        if (sanPhamChiTiet == null) {
            response.put("status", "warning");
            response.put("message", "Chi tiết sản phẩm không tồn tại!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        boolean existsInHoaDonChiTiet = hoaDonChiTietRepository.existsBySanPhamChiTiet(sanPhamChiTiet);
        if (existsInHoaDonChiTiet) {
            response.put("status", "warning");
            response.put("message", "Sản phẩm đã bán!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        hinhAnhRepository.deleteBySanPhamChiTiet(sanPhamChiTiet);
        sanPhamChiTietRepository.deleteById(id);
        capNhatSoLuongTonKhoSanPham(sanPhamChiTiet.getSanPham());

        response.put("status", "success");
        response.put("message", "Xóa sản phẩm chi tiết thành công!");
        return ResponseEntity.ok(response);
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

        // Kiểm tra xem sản phẩm có liên kết với hóa đơn chi tiết hay không
        boolean existsInHoaDonChiTiet = hoaDonChiTietRepository.existsBySanPhamChiTiet(hinhAnh.getSanPhamChiTiet());
        if (existsInHoaDonChiTiet) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không thể xóa hình ảnh vì sản phẩm đang được bán!");
        }

        // Xóa ảnh trên Cloudinary
        boolean xoaThanhCong = hinhAnhService.xoaAnhTrenCloudinary(publicId);
        if (!xoaThanhCong) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi xóa ảnh trên Cloudinary!");
        }

        // Xóa ảnh khỏi cơ sở dữ liệu
        hinhAnhRepository.delete(hinhAnh);
        return ResponseEntity.ok("Xóa hình ảnh thành công!");
    }




}
