package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.KichThuoc;
import com.example.datn_trendsetter.Repository.KichThuocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/kich-thuoc")
public class KichThuocApiController {

    @Autowired
    private KichThuocRepository kichThuocRepository;

    @PostMapping("add")
    public ResponseEntity<String> add(@RequestBody KichThuoc kichThuocRequest) {
        // Kiểm tra xem thương hiệu đã tồn tại chưa
        boolean exists = kichThuocRepository.existsByTenKichThuoc(kichThuocRequest.getTenKichThuoc());

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên kích thước đã tồn tại");
        }

        // Nếu chưa tồn tại, tạo mới
        KichThuoc kichThuoc= new KichThuoc();
        kichThuoc.setMaKichThuoc("KT-" + UUID.randomUUID().toString().substring(0, 8)); // Sinh mã ngẫu nhiên
        kichThuoc.setTenKichThuoc(kichThuocRequest.getTenKichThuoc());
        kichThuoc.setNgayTao(LocalDate.now());
        kichThuoc.setNgaySua(LocalDate.now());
        kichThuoc.setNguoiTao("admin");
        kichThuoc.setNguoiSua("admin");
        kichThuoc.setDeleted(false);
        kichThuoc.setTrangThai(kichThuocRequest.getTrangThai());

        kichThuocRepository.save(kichThuoc);
        return ResponseEntity.ok("Thêm kích thước thành công");
    }


    @PutMapping("update")
    public ResponseEntity<String> update(@RequestBody KichThuoc updatedKichThuoc) {
        KichThuoc kichThuoc = kichThuocRepository.findById(updatedKichThuoc.getId()).orElse(null);
        if (kichThuoc != null) {
            kichThuoc.setMaKichThuoc(kichThuoc.getMaKichThuoc());
            kichThuoc.setTenKichThuoc(updatedKichThuoc.getTenKichThuoc());
            kichThuoc.setNgayTao(kichThuoc.getNgayTao());
            kichThuoc.setNgaySua(LocalDate.now());
            kichThuoc.setNguoiTao(kichThuoc.getNguoiTao());
            kichThuoc.setNguoiSua(kichThuoc.getNguoiSua());
            kichThuoc.setTrangThai(kichThuoc.getTrangThai());
            kichThuoc.setDeleted(kichThuoc.getDeleted());
            kichThuocRepository.save(kichThuoc);
            return ResponseEntity.ok("Cập nhật thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy danh mục");
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        Optional<KichThuoc> kichThuoc = kichThuocRepository.findById(id);
        if (kichThuoc.isPresent()) {
            kichThuocRepository.deleteById(id);
            return ResponseEntity.ok("Xóa kích thước thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy kích thước");
    }

}
