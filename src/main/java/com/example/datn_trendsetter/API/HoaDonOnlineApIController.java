package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.HoaDonDTO;
import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Service.HoaDonOnlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/hoa-don")
public class HoaDonOnlineApIController {
    @Autowired
    private HoaDonOnlineService hoaDonOnlineService;

    // ✅ API tạo hóa đơn
    @PostMapping("/create")
    public ResponseEntity<?> createHoaDon(@RequestBody HoaDonDTO hoaDonDTO) {
        try {
            HoaDon hoaDon = hoaDonOnlineService.createHoaDon(hoaDonDTO);
            return ResponseEntity.ok(hoaDon); // Trả về hóa đơn vừa tạo
        } catch (IllegalArgumentException e) {
            e.printStackTrace(); // Ghi log lỗi chi tiết ra console
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); // Đảm bảo trả về JSON hợp lệ
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

}

