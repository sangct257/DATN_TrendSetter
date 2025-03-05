package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.DotGiamGia;
import com.example.datn_trendsetter.Repository.DotGiamGiaRepository;
import com.example.datn_trendsetter.Service.DotGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dot-giam-gia")
public class DotGiamGiaAPIController {
    @Autowired
    private DotGiamGiaRepository dotGiamGiaRepository;

    @Autowired
    private DotGiamGiaService dotGiamGiaService;

    @GetMapping("/list")
    public ResponseEntity<List<DotGiamGia>> getDotGiamGiaByTrangThai(@RequestParam(required = false) String trangThai) {
        List<DotGiamGia> dotGiamGiaList;
        if (trangThai != null && !trangThai.isEmpty()) {
            dotGiamGiaList = dotGiamGiaRepository.findByTrangThai(trangThai, Sort.by(Sort.Direction.DESC,"id"));
        } else {
            dotGiamGiaList = dotGiamGiaRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        }
        return ResponseEntity.ok().body(dotGiamGiaList);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countDotGiamGiaByTrangThai() {
        long hoatDong = dotGiamGiaRepository.countByTrangThai("Đang Hoạt Động");
        long ngungHoatDong = dotGiamGiaRepository.countByTrangThai("Ngừng Hoạt Động");
        long tong = dotGiamGiaRepository.count();

        Map<String, Long> coutMap = Map.of(
                "Đang Hoạt Động",hoatDong,
                "Ngừng Hoạt Động",ngungHoatDong,
                "Tất Cả",tong
        );

        return ResponseEntity.ok().body(coutMap);
    }

    // API để xóa mềm đợt giảm (khi nhấn vào trạng thái)
    @PutMapping("/toggle-status/{id}")
    public ResponseEntity<?> toggleDotGiamGiaStatus(@PathVariable Integer id) {
        boolean updated = dotGiamGiaService.toggleDotGiamGiaStatus(id);
        if (updated) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Cập nhật trạng thái thành công!"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Đợt Giảm Giá không tồn tại!"));
        }
    }
}
