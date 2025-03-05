package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.DotGiamGia;
import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import com.example.datn_trendsetter.Repository.DotGiamGiaRepository;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import com.example.datn_trendsetter.Service.DotGiamGiaService;
import com.example.datn_trendsetter.Service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/phieu-giam-gia")
public class PhieuGiamGiaApiController {
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;

    @GetMapping("/list")
    public ResponseEntity<List<PhieuGiamGia>> getPhieuGiamGiaByTrangThai(@RequestParam(required = false) String trangThai) {
        List<PhieuGiamGia> phieuGiamGiaList;
        if (trangThai != null && !trangThai.isEmpty()) {
            phieuGiamGiaList = phieuGiamGiaRepository.findByTrangThai(trangThai, Sort.by(Sort.Direction.DESC,"id"));
        } else {
            phieuGiamGiaList = phieuGiamGiaRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        }
        return ResponseEntity.ok().body(phieuGiamGiaList);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countDotGiamGiaByTrangThai() {
        long hoatDong = phieuGiamGiaRepository.countByTrangThai("Đang Hoạt Động");
        long ngungHoatDong = phieuGiamGiaRepository.countByTrangThai("Ngừng Hoạt Động");
        long tong = phieuGiamGiaRepository.count();

        Map<String, Long> coutMap = Map.of(
                "Đang Hoạt Động",hoatDong,
                "Ngừng Hoạt Động",ngungHoatDong,
                "Tất Cả",tong
        );

        return ResponseEntity.ok().body(coutMap);
    }

    // API để xóa mềm đợt giảm (khi nhấn vào trạng thái)
    @PutMapping("/toggle-status/{id}")
    public ResponseEntity<?> togglePhieuGiamGiaStatus(@PathVariable Integer id) {
        boolean updated = phieuGiamGiaService.togglePhieuGiamGiaStatus(id);
        if (updated) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Cập nhật trạng thái thành công!"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Phiếu Giảm Giá không tồn tại!"));
        }
    }
}
