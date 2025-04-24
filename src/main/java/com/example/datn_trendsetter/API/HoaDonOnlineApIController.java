package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.HoaDonDTO;
import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Service.HoaDonOnlineService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/hoa-don")
public class HoaDonOnlineApIController {
    @Autowired
    private HoaDonOnlineService hoaDonOnlineService;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    // ✅ API tạo hóa đơn
    @PostMapping("/create")
    public ResponseEntity<?> createHoaDon(@RequestBody HoaDonDTO hoaDonDTO, HttpSession session) {
        try {
            HoaDon hoaDon = hoaDonOnlineService.createHoaDon(hoaDonDTO,session);
            return ResponseEntity.ok(hoaDon); // Trả về hóa đơn vừa tạo
        } catch (IllegalArgumentException e) {
            e.printStackTrace(); // Ghi log lỗi chi tiết ra console
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); // Đảm bảo trả về JSON hợp lệ
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    @PatchMapping("/{orderId}/update-status")
    public ResponseEntity<?> updateHoaDonStatus(@PathVariable String orderId, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("trangThai");
            if (status == null || status.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Trạng thái không được để trống"));
            }

            HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(orderId);
            if (hoaDon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Không tìm thấy hóa đơn"));
            }

            hoaDon.setTrangThai(status);
            hoaDonRepository.save(hoaDon);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

}

