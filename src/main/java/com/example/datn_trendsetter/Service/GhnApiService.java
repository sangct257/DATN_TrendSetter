package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import jakarta.persistence.EntityNotFoundException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

@Service
public class GhnApiService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Value("${ghn.api.token}")
    private String ghnApiToken;

    @Value("${ghn.shop.id}")
    private String ghnShopId;

    private static final String BASE_URL = "https://online-gateway.ghn.vn/shiip/public-api/";
    private static final Logger logger = LoggerFactory.getLogger(GhnApiService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders createHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnApiToken);
        headers.set("ShopId", ghnShopId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }



    public ResponseEntity<String> sendRequest(String endpoint, HttpMethod method, Object body) {
        String url = BASE_URL + endpoint;
        HttpHeaders headers = createHeaders();

        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, request, String.class);
            return response;
        } catch (Exception e) {
            logger.error("Lỗi khi gọi API GHN: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi API GHN: " + e.getMessage());
        }
    }


    public ResponseEntity<String> getProvinces() {
        return sendRequest("master-data/province", HttpMethod.GET, null);
    }

    public ResponseEntity<String> getDistricts(int provinceId) {
        Map<String, Object> body = new HashMap<>();
        body.put("province_id", provinceId);
        return sendRequest("master-data/district", HttpMethod.POST, body);
    }

    public ResponseEntity<String> getWards(int districtId) {
        Map<String, Object> body = new HashMap<>();
        body.put("district_id", districtId);
        return sendRequest("master-data/ward", HttpMethod.POST, body);
    }

    public ResponseEntity<String> calculateShippingFee(Map<String, Object> requestBody) {
        String[] requiredFields = {
                "from_district_id", "to_district_id", "to_ward_code",
                "weight", "length", "width", "height", "insurance_value"
        };

        for (String field : requiredFields) {
            if (!requestBody.containsKey(field)) {
                return ResponseEntity.badRequest()
                        .body("Thiếu thông tin cần thiết: " + field);
            }
        }

        return sendRequest("v2/shipping-order/fee", HttpMethod.POST, requestBody);
    }

    public ResponseEntity<String> calculateShippingFee(int toDistrictId, String toWardCode) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("service_id", 53320);
        requestBody.put("insurance_value", 500000);
        requestBody.put("coupon",null);
        requestBody.put("from_district_id", 1542);
        requestBody.put("to_district_id", toDistrictId);
        requestBody.put("to_ward_code", toWardCode);
        requestBody.put("weight", 500);
        requestBody.put("length", 10);
        requestBody.put("width", 10);
        requestBody.put("height", 10);

        return calculateShippingFee(requestBody);
    }

    public ResponseEntity<String> calculateShippingFeeByHoaDonId(Integer hoaDonId) {
        if (hoaDonId == null) {
            return ResponseEntity.badRequest().body("Hóa đơn ID không được null!");
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn!"));

        if (hoaDon.getThanhPho() == null || hoaDon.getHuyen() == null || hoaDon.getPhuong() == null) {
            return ResponseEntity.badRequest().body("Địa chỉ giao hàng chưa đầy đủ!");
        }

        // Lấy ID tỉnh/thành phố
        Integer provinceId = getProvinceIdByName(hoaDon.getThanhPho());
        if (provinceId == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy mã tỉnh/thành phố!");
        }

        // Lấy ID quận/huyện
        Integer toDistrictId = getDistrictIdByName(hoaDon.getHuyen(), provinceId);
        if (toDistrictId == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy mã quận/huyện!");
        }

        // Lấy mã phường/xã
        String toWardCode = getWardCodeByName(hoaDon.getPhuong(), toDistrictId);
        if (toWardCode == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy mã phường/xã!");
        }

        return calculateShippingFee(toDistrictId, toWardCode);
    }

    private Integer getProvinceIdByName(String provinceName) {
        ResponseEntity<String> response = getProvinces(); // API lấy danh sách tỉnh/thành phố
        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            JSONArray provinces = jsonResponse.getJSONArray("data");
            for (int i = 0; i < provinces.length(); i++) {
                JSONObject province = provinces.getJSONObject(i);
                if (provinceName.equalsIgnoreCase(province.getString("ProvinceName"))) {
                    return province.getInt("ProvinceID");
                }
            }
        }
        return null;
    }

    private Integer getDistrictIdByName(String districtName, Integer provinceId) {
        ResponseEntity<String> response = getDistricts(provinceId); // Lấy danh sách quận/huyện theo tỉnh
        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            JSONArray districts = jsonResponse.getJSONArray("data");
            for (int i = 0; i < districts.length(); i++) {
                JSONObject district = districts.getJSONObject(i);
                if (districtName.equalsIgnoreCase(district.getString("DistrictName"))) {
                    return district.getInt("DistrictID");
                }
            }
        }
        return null;
    }

    private String getWardCodeByName(String wardName, Integer districtId) {
        ResponseEntity<String> response = getWards(districtId); // Lấy danh sách phường/xã theo quận/huyện
        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            JSONArray wards = jsonResponse.getJSONArray("data");
            for (int i = 0; i < wards.length(); i++) {
                JSONObject ward = wards.getJSONObject(i);
                if (wardName.equalsIgnoreCase(ward.getString("WardName"))) {
                    return ward.getString("WardCode");
                }
            }
        }
        return null;
    }


}
