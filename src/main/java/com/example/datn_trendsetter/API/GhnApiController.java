package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Service.GhnApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ghn")
public class GhnApiController {

    @Autowired
    private GhnApiService ghnApiService;

    @GetMapping("/provinces")
    public ResponseEntity<String> getProvinces() {
        return ghnApiService.getProvinces();
    }

    // API lấy danh sách Quận/Huyện
    @GetMapping("/districts")
    public ResponseEntity<String> getDistricts(@RequestParam("province_id") int provinceId) {
        return ghnApiService.getDistricts(provinceId);
    }

    // API lấy danh sách Phường/Xã
    @GetMapping("/wards")
    public ResponseEntity<String> getWards(@RequestParam("district_id") int districtId) {
        return ghnApiService.getWards(districtId);
    }


    @GetMapping("/shop-address")
    public ResponseEntity<String> getShopAddressByShopId(@RequestParam(value = "shop_id", required = false) String shopId) {
        String addressId = ghnApiService.getShopAddressByShopId(shopId);
        if (addressId != null) {
            return ResponseEntity.ok(addressId);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy địa chỉ của shop");
        }
    }

    @GetMapping("/calculate-fee")
    public ResponseEntity<String> calculateFee(
            @RequestParam("to_district_id") int toDistrictId,
            @RequestParam("to_ward_code") String toWardCode,
            @RequestParam("weight") int weight) {
        return ghnApiService.calculateShippingFee(toDistrictId, toWardCode, weight);
    }



}