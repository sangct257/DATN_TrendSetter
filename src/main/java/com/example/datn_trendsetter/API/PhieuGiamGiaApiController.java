package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.PhieuGiamGiaDTO;
import com.example.datn_trendsetter.Entity.DotGiamGia;
import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import com.example.datn_trendsetter.Entity.SanPham;
import com.example.datn_trendsetter.Repository.DotGiamGiaRepository;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import com.example.datn_trendsetter.Service.DotGiamGiaService;
import com.example.datn_trendsetter.Service.PhieuGiamGiaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/phieu-giam-gia")
@RequiredArgsConstructor
public class PhieuGiamGiaApiController {
    private final PhieuGiamGiaService phieuGiamGiaService;
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @GetMapping
    public ResponseEntity<List<PhieuGiamGiaDTO>> getPhieuGiamGiaByTrangThai(@RequestParam(required = false) String trangThai) {
        List<PhieuGiamGiaDTO> phieuGiamGiaList;

        if (trangThai != null && !trangThai.isEmpty()) {
            // Nếu có trạng thái, tìm phiếu giảm giá theo trạng thái
            phieuGiamGiaList = phieuGiamGiaService.getPhieuGiamGiaByTrangThai(trangThai);
        } else {
            // Nếu không có trạng thái, lấy tất cả phiếu giảm giá
            phieuGiamGiaList = phieuGiamGiaService.getAllPhieuGiamGia();
        }

        return ResponseEntity.ok(phieuGiamGiaList);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countDotGiamGiaByTrangThai() {
        long hoatDong = phieuGiamGiaRepository.countByTrangThaiAndDeletedFalse("Đang Hoạt Động");
        long ngungHoatDong = phieuGiamGiaRepository.countByTrangThaiAndDeletedFalse("Ngừng Hoạt Động");
        long sapDienRa = phieuGiamGiaRepository.countByTrangThaiAndDeletedFalse("Sắp Diễn Ra");
        long tong = phieuGiamGiaRepository.countByDeletedFalse();

        Map<String, Long> countMap = Map.of(
                "Đang Hoạt Động", hoatDong,
                "Ngừng Hoạt Động", ngungHoatDong,
                "Sắp Diễn Ra", sapDienRa,
                "Tất Cả", tong
        );

        return ResponseEntity.ok().body(countMap);
    }


    // API để update trạng thái phiếu giảm giá  (khi nhấn vào trạng thái)
    @PutMapping("/toggle-status/{id}")
    public ResponseEntity<?> togglePhieuGiamGiaStatus(@PathVariable Integer id) {
        boolean updated = phieuGiamGiaService.togglePhieuGiamGiaStatus(id);
        if (updated) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Cập nhật trạng thái thành công!"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Phiếu giảm giá không tồn tại!"));
        }
    }

    @PostMapping("/add/multiple")
    public ResponseEntity<PhieuGiamGia> addPhieuGiamGiaForMultipleCustomers(
            @RequestBody Map<String, Object> requestBody) {
        // Chuyển đổi từ map nhận được từ requestBody sang đối tượng DTO
        PhieuGiamGiaDTO dto = new ObjectMapper().convertValue(requestBody.get("phieuGiamGia"), PhieuGiamGiaDTO.class);

        // Thêm phiếu giảm giá
        PhieuGiamGia phieuGiamGia = phieuGiamGiaService.addPhieuGiamGiaForMultipleCustomers(dto);
        return ResponseEntity.ok(phieuGiamGia);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable Integer id) {
        return phieuGiamGiaService.getDetail(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        phieuGiamGiaService.deletePhieuGiamGia(id);
        return ResponseEntity.ok("Xóa mềm thành công!");
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<PhieuGiamGia> updatePhieuGiamGia(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> requestBody) {
        return ResponseEntity.ok(phieuGiamGiaService.updatePhieuGiamGia(id, requestBody));
    }

}