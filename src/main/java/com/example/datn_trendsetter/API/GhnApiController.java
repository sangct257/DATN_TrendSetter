package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Service.GhnApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ghn")
public class GhnApiController {

    @Autowired
    private GhnApiService ghnApiService;


    @GetMapping("/provinces")
    public ResponseEntity<String> getProvinces() {
        return ghnApiService.getProvinces();
    }

    // Sửa lại phương thức getDistricts, kiểm tra tham số đúng trong URL
    @GetMapping("/districts")
    public ResponseEntity<String> getDistricts(@RequestParam("province_id") int provinceId) {
        if (provinceId <= 0) {
            return ResponseEntity.badRequest().body("Province ID không hợp lệ.");
        }
        return ghnApiService.getDistricts(provinceId);
    }

    @GetMapping("/wards")
    public ResponseEntity<String> getWards(@RequestParam("district_id") int districtId) {
        return ghnApiService.getWards(districtId);
    }


    @PostMapping("/calculate-shipping-fee")
    public ResponseEntity<String> calculateShippingFee(@RequestBody Map<String, Object> requestBody) {
        return ghnApiService.calculateShippingFee(requestBody);
    }


}

