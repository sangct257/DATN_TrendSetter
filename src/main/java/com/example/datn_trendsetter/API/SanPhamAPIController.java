package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.ProductDetailDTO;
import com.example.datn_trendsetter.DTO.SanPhamChiTietDTO;
import com.example.datn_trendsetter.DTO.SanPhamChiTietRequest;
import com.example.datn_trendsetter.DTO.SanPhamDTO;
import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import com.example.datn_trendsetter.Service.HinhAnhService;
import com.example.datn_trendsetter.Service.SanPhamChiTietService;
import com.example.datn_trendsetter.Service.SanPhamService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
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
        try {
            SanPham sanPham = sanPhamService.addSanPham(sanPhamDTO);
            return ResponseEntity.ok(sanPham);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi thêm sản phẩm");
        }
    }

    @PutMapping("/update-san-pham/{id}")
    public ResponseEntity<?> updateSanPham(@PathVariable Integer id, @RequestBody SanPhamDTO sanPhamDTO) {
        try {
            return ResponseEntity.ok(sanPhamService.updateSanPham(id, sanPhamDTO));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
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
    public ResponseEntity<?> capNhatChiTietSanPham(@RequestBody SanPhamChiTietRequest request) {
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
            SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(sanPhamChiTietId).orElse(null);
            if (sanPhamChiTiet == null) {
                return ResponseEntity.badRequest().body("Sản phẩm không tồn tại!");
            }

            List<String> imageUrls = hinhAnhService.uploadImages(files);
            List<String> savedImageUrls = new ArrayList<>();

            for (String url : imageUrls) {
                HinhAnh hinhAnh = new HinhAnh();
                hinhAnh.setUrlHinhAnh(url);
                hinhAnh.setSanPhamChiTiet(sanPhamChiTiet);
                hinhAnh.setNgayTao(LocalDate.now());
                hinhAnh.setTrangThai("ACTIVE");
                hinhAnh.setDeleted(false);
                hinhAnhRepository.save(hinhAnh);
                savedImageUrls.add(url);
            }

            return ResponseEntity.ok(savedImageUrls); // Trả về danh sách ảnh vừa upload
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Lỗi upload: " + e.getMessage());
        }
    }

    @DeleteMapping("/xoa/{hinhAnhId}")
    public ResponseEntity<String> xoaHinhAnh(@PathVariable Integer hinhAnhId) {
        System.out.println("Nhận yêu cầu xóa ảnh ID: " + hinhAnhId);

        Optional<HinhAnh> optionalHinhAnh = hinhAnhRepository.findById(hinhAnhId);

        if (optionalHinhAnh.isEmpty()) {
            System.out.println("Hình ảnh không tồn tại!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hình ảnh không tồn tại!");
        }

        HinhAnh hinhAnh = optionalHinhAnh.get();
        hinhAnhRepository.delete(hinhAnh);
        System.out.println("Xóa thành công ảnh ID: " + hinhAnhId);

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
        if (sanPham.getSanPhamChiTiet() != null && !sanPham.getSanPhamChiTiet().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không thể xóa vì còn biến thể sản phẩm");
        }

        // Kiểm tra nếu sản phẩm có trong hóa đơn
        boolean coTrongHoaDon = sanPham.getSanPhamChiTiet().stream()
                .anyMatch(spct -> hoaDonChiTietRepository.existsBySanPhamChiTiet(spct));

        if (coTrongHoaDon) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không thể xóa vì sản phẩm đã có trong hóa đơn");
        }

        // Xóa mềm (nếu có cột deleted)
        sanPham.setTrangThai("Không Hoạt Động");
        sanPham.setDeleted(true);
        sanPhamRepository.save(sanPham);

        return ResponseEntity.ok("Xóa sản phẩm thành công");
    }

}
