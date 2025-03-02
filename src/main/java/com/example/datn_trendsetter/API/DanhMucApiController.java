package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.DanhMuc;
import com.example.datn_trendsetter.Entity.ThuongHieu;
import com.example.datn_trendsetter.Repository.DanhMucRepository;
import com.example.datn_trendsetter.Repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/danh-muc")
public class DanhMucApiController {

    @Autowired
    private DanhMucRepository danhMucRepository;

    @PostMapping("add")
    public ResponseEntity<String> add(@RequestBody DanhMuc danhMucRequest) {
        // Kiểm tra xem thương hiệu đã tồn tại chưa
        boolean exists = danhMucRepository.existsByTenDanhMuc(danhMucRequest.getTenDanhMuc());

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên danh mục đã tồn tại");
        }

        // Nếu chưa tồn tại, tạo mới
        DanhMuc danhMuc = new DanhMuc();
        danhMuc.setMaDanhMuc("DM-" + UUID.randomUUID().toString().substring(0, 8)); // Sinh mã ngẫu nhiên
        danhMuc.setTenDanhMuc(danhMucRequest.getTenDanhMuc());
        danhMuc.setNgayTao(LocalDate.now());
        danhMuc.setNgaySua(LocalDate.now());
        danhMuc.setNguoiTao("admin");
        danhMuc.setNguoiSua("admin");
        danhMuc.setDeleted(false);
        danhMuc.setTrangThai(danhMucRequest.getTrangThai());

        danhMucRepository.save(danhMuc);
        return ResponseEntity.ok("Thêm danh mục thành công");
    }


    @PostMapping("update")
    public ResponseEntity<String> update(@RequestBody DanhMuc updatedDanhMuc) {
        DanhMuc danhMuc = danhMucRepository.findById(updatedDanhMuc.getId()).orElse(null);
        if (danhMuc != null) {
            danhMuc.setMaDanhMuc(danhMuc.getMaDanhMuc());
            danhMuc.setTenDanhMuc(danhMuc.getTenDanhMuc());
            danhMuc.setNgayTao(danhMuc.getNgayTao());
            danhMuc.setNgaySua(LocalDate.now());
            danhMuc.setNguoiTao(danhMuc.getNguoiTao());
            danhMuc.setNguoiSua(danhMuc.getNguoiSua());
            danhMuc.setTrangThai(danhMuc.getTrangThai());
            danhMuc.setDeleted(danhMuc.getDeleted());
            danhMucRepository.save(danhMuc);
            return ResponseEntity.ok("Cập nhật thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy danh mục");
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        Optional<DanhMuc> danhMuc = danhMucRepository.findById(id);
        if (danhMuc.isPresent()) {
            danhMucRepository.deleteById(id);
            return ResponseEntity.ok("Xóa danh mục thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy danh mục");
    }

}
