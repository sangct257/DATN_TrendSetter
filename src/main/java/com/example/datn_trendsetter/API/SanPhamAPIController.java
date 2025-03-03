package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.ProductDetailDTO;
import com.example.datn_trendsetter.DTO.SanPhamChiTietDTO;
import com.example.datn_trendsetter.DTO.SanPhamDTO;
import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import com.example.datn_trendsetter.Service.HinhAnhService;
import com.example.datn_trendsetter.Service.SanPhamService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

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

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addSanPham(@RequestBody SanPhamDTO sanPhamDTO) {
        return sanPhamService.addSanPham(sanPhamDTO);
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
    public ResponseEntity<String> addMauSacKichThuoc(@RequestBody ProductDetailDTO request) {
        Optional<SanPham> sanPhamOpt = sanPhamRepository.findById(request.getSanPhamId());
        if (!sanPhamOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Sản phẩm không tồn tại");
        }
        SanPham sanPham = sanPhamOpt.get();

        for (Integer mauSacId : request.getMauSacIds()) {
            for (Integer kichThuocId : request.getKichThuocIds()) {
                boolean exists = sanPhamChiTietRepository.existsBySanPhamAndMauSacIdAndKichThuocId(
                        sanPham, mauSacId, kichThuocId);
                if (!exists) {
                    SanPhamChiTiet chiTiet = new SanPhamChiTiet();
                    chiTiet.setSanPham(sanPham);
                    chiTiet.setMauSac(mauSacRepository.findById(mauSacId).orElse(null));
                    chiTiet.setKichThuoc(kichThuocRepository.findById(kichThuocId).orElse(null));
                    sanPhamChiTietRepository.save(chiTiet);
                }
            }
        }
        return ResponseEntity.ok("Thêm thành công!");
    }

    @PutMapping("/update")
    public ResponseEntity<?> capNhatChiTietSanPham(@RequestBody SanPhamChiTietDTO request) {
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(request.getId()).orElse(null);
        if (sanPhamChiTiet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chi tiết sản phẩm không tồn tại!");
        }


        sanPhamChiTiet.setSoLuong(request.getSoLuong());
        sanPhamChiTiet.setGia(request.getGia().floatValue());
        sanPhamChiTiet.setTrangThai(request.getSoLuong() > 0 ? "Còn Hàng" : "Hết Hàng");
        sanPhamChiTietRepository.save(sanPhamChiTiet);



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
        sanPham.setTrangThai(tongSoLuong > 0 ? "Đang Hoạt Động" : "Không Hoạt Động");
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
        String publicId = hinhAnh.getPublicId(); // Lấy đúng public_id

        boolean xoaThanhCong = hinhAnhService.xoaAnhTrenCloudinary(publicId);
        if (!xoaThanhCong) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi xóa ảnh trên Cloudinary!");
        }

        // Xóa hoàn toàn khỏi database sau khi xóa trên Cloudinary thành công
        hinhAnhRepository.delete(hinhAnh);


        return ResponseEntity.ok("Xóa hình ảnh thành công!");
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<?> xoaSanPham(@PathVariable Integer id) {
        Optional<SanPham> optionalSanPham = sanPhamRepository.findById(id);
        if (optionalSanPham.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sản phẩm không tồn tại");
        }

        SanPham sanPham = optionalSanPham.get();

        // Kiểm tra nếu sản phẩm có biến thể
        List<SanPhamChiTiet> sanPhamChiTietList = sanPham.getSanPhamChiTiet();
        if (sanPhamChiTietList != null && !sanPhamChiTietList.isEmpty()) {
            boolean coTrongHoaDon = sanPhamChiTietList.stream()
                    .anyMatch(spct -> hoaDonChiTietRepository.existsBySanPhamChiTiet(spct));

            if (coTrongHoaDon) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Không thể xóa vì sản phẩm đã có trong hóa đơn");
            }

            // Xóa tất cả biến thể sản phẩm trước khi xóa sản phẩm chính
            sanPhamChiTietRepository.deleteAll(sanPhamChiTietList);
        }

        // Xóa quan hệ với danh mục, thương hiệu, xuất xứ, chất liệu trước khi xóa
        sanPham.setDanhMuc(null);
        sanPham.setThuongHieu(null);
        sanPham.setXuatXu(null);
        sanPham.setChatLieu(null);

        // Xóa sản phẩm
        sanPhamRepository.delete(sanPham);

        return ResponseEntity.ok("Xóa sản phẩm thành công");
    }


}
