package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.ThuongHieu;
import com.example.datn_trendsetter.Repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/thuong-hieu")
public class ThuongHieuApiController {

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @PostMapping("add")
    public ResponseEntity<String> addThuongHieu(@RequestBody ThuongHieu thuongHieuRequest) {
        // Kiểm tra xem thương hiệu đã tồn tại chưa
        boolean exists = thuongHieuRepository.existsByTenThuongHieu(thuongHieuRequest.getTenThuongHieu());

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên thương hiệu đã tồn tại");
        }

        // Nếu chưa tồn tại, tạo mới
        ThuongHieu thuongHieu = new ThuongHieu();
        thuongHieu.setMaThuongHieu("TH-" + UUID.randomUUID().toString().substring(0, 8)); // Sinh mã ngẫu nhiên
        thuongHieu.setTenThuongHieu(thuongHieuRequest.getTenThuongHieu());
        thuongHieu.setNgayTao(LocalDate.now());
        thuongHieu.setNgaySua(LocalDate.now());
        thuongHieu.setNguoiTao("admin");
        thuongHieu.setNguoiSua("admin");
        thuongHieu.setTrangThai(thuongHieuRequest.getTrangThai());

        thuongHieuRepository.save(thuongHieu);
        return ResponseEntity.ok("Thêm thương hiệu thành công");
    }


    @PostMapping("update")
    public ResponseEntity<String> updateThuongHieu(@RequestBody ThuongHieu updatedBrand) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(updatedBrand.getId()).orElse(null);
        if (thuongHieu != null) {
            thuongHieu.setMaThuongHieu(thuongHieu.getMaThuongHieu());
            thuongHieu.setTenThuongHieu(thuongHieu.getTenThuongHieu());
            thuongHieu.setNgayTao(thuongHieu.getNgayTao());
            thuongHieu.setNgaySua(LocalDate.now());
            thuongHieu.setNguoiTao(thuongHieu.getNguoiTao());
            thuongHieu.setNguoiSua(thuongHieu.getNguoiSua());
            thuongHieu.setTrangThai(thuongHieu.getTrangThai());
            thuongHieuRepository.save(thuongHieu);
            return ResponseEntity.ok("Cập nhật thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy thương hiệu");
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> deleteThuongHieu(@PathVariable Integer id) {
        Optional<ThuongHieu> thuongHieu = thuongHieuRepository.findById(id);
        if (thuongHieu.isPresent()) {
            thuongHieuRepository.deleteById(id);
            return ResponseEntity.ok("Xóa thương hiệu thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy thương hiệu");
    }

}
