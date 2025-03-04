package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Service.HoaDonService;
import com.example.datn_trendsetter.Service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hoa-don")
public class HoaDonApiController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private ShopService shopService;

    @PostMapping("/create-hoa-don")
    public ResponseEntity<?> createHoaDon(@RequestBody(required = false) HoaDon hoaDon) {
        try {
            if (hoaDon == null) {
                throw new Exception("Dữ liệu hóa đơn không hợp lệ hoặc thiếu.");
            }
            return ResponseEntity.ok(shopService.createHoaDon(hoaDon));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


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

    // API lấy danh sách hóa đơn theo trạng thái
    @GetMapping("/filter")
    public List<HoaDon> getHoaDonByTrangThai(@RequestParam(required = false) String trangThai) {
        if (trangThai == null || trangThai.isEmpty()) {
            return hoaDonRepository.findAll(); // Trả về tất cả hóa đơn nếu không lọc theo trạng thái
        }
        return hoaDonRepository.findByTrangThai(trangThai);
    }

    // API đếm số lượng hóa đơn theo từng trạng thái
    @GetMapping("/count")
    public Map<String, Long> countHoaDonByTrangThai() {
        List<HoaDon> hoaDons = hoaDonRepository.findAll();
        return hoaDons.stream()
                .collect(Collectors.groupingBy(HoaDon::getTrangThai, Collectors.counting()));
    }

}
