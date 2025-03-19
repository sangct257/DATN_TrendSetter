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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/mau-sac")
public class MauSacApiController {
    @Autowired
    private MauSacRepository mauSacRepository;

    @PostMapping("add")
    public ResponseEntity<Map<String, String>> add(@RequestBody MauSac mauSacRequest) {
        Map<String, String> response = new HashMap<>();

        boolean exists = mauSacRepository.existsByTenMauSac(mauSacRequest.getTenMauSac());
        if (exists) {
            response.put("error", "Tên màu sắc đã tồn tại");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        MauSac mauSac = new MauSac();
        mauSac.setMaMauSac(mauSacRequest.getMaMauSac());
        mauSac.setTenMauSac(mauSacRequest.getTenMauSac());
        mauSac.setNgayTao(LocalDate.now());
        mauSac.setNgaySua(LocalDate.now());
        mauSac.setNguoiTao("admin");
        mauSac.setNguoiSua("admin");
        mauSac.setDeleted(false);
        mauSac.setTrangThai(mauSacRequest.getTrangThai());

        mauSacRepository.save(mauSac);

        response.put("message", "Thêm màu sắc thành công");
        return ResponseEntity.ok(response);
    }



    @PutMapping("update")
    public ResponseEntity<Map<String, String>> update(@RequestBody MauSac updatedMauSac) {
        Map<String, String> response = new HashMap<>();

        if (updatedMauSac.getId() == null) {
            response.put("error", "ID không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        MauSac mauSac = mauSacRepository.findById(updatedMauSac.getId()).orElse(null);
        if (mauSac == null || mauSac.getDeleted()) {
            response.put("error", "Không tìm thấy màu sắc hoặc đã bị xóa");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Kiểm tra trùng lặp tên màu sắc
        boolean exists = mauSacRepository.existsByTenMauSac(updatedMauSac.getTenMauSac());
        if (exists && !mauSac.getTenMauSac().equalsIgnoreCase(updatedMauSac.getTenMauSac())) {
            response.put("error", "Tên màu sắc đã tồn tại");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Cập nhật thông tin màu sắc
        mauSac.setMaMauSac(updatedMauSac.getMaMauSac());
        mauSac.setTenMauSac(updatedMauSac.getTenMauSac());
        mauSac.setNgaySua(LocalDate.now());
        mauSac.setNguoiSua(updatedMauSac.getNguoiSua() != null ? updatedMauSac.getNguoiSua() : "admin");
        mauSac.setTrangThai(updatedMauSac.getTrangThai());

        mauSacRepository.save(mauSac);

        response.put("message", "Cập nhật thành công");
        return ResponseEntity.ok(response);
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
