package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.KichThuoc;
import com.example.datn_trendsetter.Entity.MauSac;
import com.example.datn_trendsetter.Repository.MauSacRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.MutableAttributeSet;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/mau-sac")
public class MauSacApiController {
    @Autowired
    private MauSacRepository mauSacRepository;


    @PostMapping("add")
    public ResponseEntity<String> add(@RequestBody MauSac mauSacRequest) {
        // Kiểm tra xem thương hiệu đã tồn tại chưa
        boolean exists = mauSacRepository.existsByTenMauSac(mauSacRequest.getTenMauSac());

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên màu sắc đã tồn tại");
        }

        // Nếu chưa tồn tại, tạo mới
        MauSac mauSac= new MauSac();
        mauSac.setMaMauSac("MS-" + UUID.randomUUID().toString().substring(0, 8)); // Sinh mã ngẫu nhiên
        mauSac.setTenMauSac(mauSacRequest.getTenMauSac());
        mauSac.setNgayTao(LocalDate.now());
        mauSac.setNgaySua(LocalDate.now());
        mauSac.setNguoiTao("admin");
        mauSac.setNguoiSua("admin");
        mauSac.setDeleted(false);
        mauSac.setTrangThai(mauSacRequest.getTrangThai());

        mauSacRepository.save(mauSac);
        return ResponseEntity.ok("Thêm màu sắc thành công");
    }


    @PostMapping("update")
    public ResponseEntity<String> update(@RequestBody MauSac updatedMauSac) {
        MauSac mauSac = mauSacRepository.findById(updatedMauSac.getId()).orElse(null);
        if (mauSac != null) {
            mauSac.setMaMauSac(mauSac.getMaMauSac());
            mauSac.setTenMauSac(updatedMauSac.getTenMauSac());
            mauSac.setNgayTao(mauSac.getNgayTao());
            mauSac.setNgaySua(LocalDate.now());
            mauSac.setNguoiTao(mauSac.getNguoiTao());
            mauSac.setNguoiSua(mauSac.getNguoiSua());
            mauSac.setTrangThai(updatedMauSac.getTrangThai());
            mauSac.setDeleted(mauSac.getDeleted());
            mauSacRepository.save(mauSac);
            return ResponseEntity.ok("Cập nhật thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy danh mục");
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        Optional<MauSac> mauSac = mauSacRepository.findById(id);
        if (mauSac.isPresent()) {
            mauSacRepository.deleteById(id);
            return ResponseEntity.ok("Xóa màu thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy màu");
    }

}
