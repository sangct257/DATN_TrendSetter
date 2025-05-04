package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.PhieuGiamGiaDTO;
import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import com.example.datn_trendsetter.Component.PhieuGiamGiaScheduler;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import com.example.datn_trendsetter.Service.PhieuGiamGiaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<?> togglePhieuGiamGiaStatus(@PathVariable Integer id, HttpSession session) {
        Optional<PhieuGiamGia> phieuGiamGiaOpt = phieuGiamGiaRepository.findById(id);

        if (phieuGiamGiaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Phiếu giảm giá không tồn tại!"));
        }

        PhieuGiamGia pgg = phieuGiamGiaOpt.get();

        // Kiểm tra vai trò ADMIN trong session
        List<String> roles = (List<String>) session.getAttribute("rolesNhanVien");
        if (roles == null || !roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Bạn không có quyền thay đổi trạng thái phiếu giảm giá!"));
        }

        // Kiểm tra số lượt sử dụng của phiếu giảm giá
        if (pgg.getSoLuotSuDung() == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Phiếu giảm giá đã hết lượt sử dụng!"));
        }

        // Kiểm tra ngày bắt đầu và ngày kết thúc
        LocalDate currentDate = LocalDate.now();
        if (pgg.getNgayBatDau().isAfter(currentDate)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Phiếu giảm giá chưa bắt đầu!"));
        }
        if (pgg.getNgayKetThuc().isBefore(currentDate)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Phiếu giảm giá đã hết hạn!"));
        }

        // Nếu tất cả điều kiện hợp lệ, cho phép thay đổi trạng thái
        pgg.setTrangThai("Đang Hoạt Động".equalsIgnoreCase(pgg.getTrangThai()) ? "Ngừng Hoạt Động" : "Đang Hoạt Động");
        PhieuGiamGiaScheduler.markAsEditedByAdmin(pgg.getId());
        phieuGiamGiaRepository.save(pgg);

        return ResponseEntity.ok(Collections.singletonMap("message", "Cập nhật trạng thái thành công (ADMIN)!"));
    }




    @PostMapping("/add/multiple")
    public ResponseEntity<PhieuGiamGia> addPhieuGiamGiaForMultipleCustomers(
            @RequestBody Map<String, Object> requestBody , HttpSession session) throws Exception {
        // Chuyển đổi từ map nhận được từ requestBody sang đối tượng DTO
        PhieuGiamGiaDTO dto = new ObjectMapper().convertValue(requestBody.get("phieuGiamGia"), PhieuGiamGiaDTO.class);

        // Thêm phiếu giảm giá
        PhieuGiamGia phieuGiamGia = phieuGiamGiaService.addPhieuGiamGiaForMultipleCustomers(dto,session);
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
    public ResponseEntity<?> updatePhieuGiamGia(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {
        try {
            PhieuGiamGia result = phieuGiamGiaService.updatePhieuGiamGia(id, requestBody, session);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }


}