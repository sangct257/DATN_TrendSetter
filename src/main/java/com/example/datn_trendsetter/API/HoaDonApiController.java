package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/hoa-don")
public class HoaDonApiController {

    @Autowired
    private HoaDonService hoaDonService;

    @PutMapping("/toggle-delivery/{hoaDonId}")
    public ResponseEntity<Map<String, String>> toggleDelivery(@PathVariable Integer hoaDonId, @RequestParam boolean delivery) {
        System.out.println("Nhận request: HoaDonId = " + hoaDonId + ", Delivery = " + delivery);

        Map<String, String> response = new HashMap<>();

        if (hoaDonId == null || hoaDonId <= 0) {
            response.put("errorMessage", "ID không hợp lệ");
            return ResponseEntity.badRequest().body(response);
        }

        boolean updated = hoaDonService.updateLoaiHoaDon(hoaDonId, delivery);

        if (updated) {
            response.put("successMessage", delivery ? "Đã chuyển thành Giao Hàng" : "Đã chuyển thành Tại Quầy");
            return ResponseEntity.ok(response);
        } else {
            response.put("errorMessage", "Hóa đơn không tồn tại");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


}
