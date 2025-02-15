package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.HoaDon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class GHNService {
    @Value("${ghn.api.url}")
    private String ghnApiUrl;

    @Value("${ghn.api.token}")
    private String ghnApiToken;

    @Value("${ghn.shop.id}")
    private String shopId;

    private final RestTemplate restTemplate;

    public GHNService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    public Map<String, Object> calculateShippingFee(int fromDistrict, int toDistrict) {
        String url = ghnApiUrl + "/v2/shipping-order/fee";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnApiToken);
        headers.set("Content-Type", "application/json");

        Map<String, Object> request = new HashMap<>();
        request.put("shop_id", Integer.parseInt(shopId));
        request.put("from_district_id", fromDistrict);
        request.put("to_district_id", toDistrict);
        request.put("service_id", 53320); // Dịch vụ GHN (VD: 53320 là dịch vụ chuẩn)
        request.put("insurance_value", 1000000);
        request.put("coupon", null);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        return response.getBody();
    }

//    public Map<String, Object> createOrder(HoaDon hoaDon) {
//        String url = ghnApiUrl + "/v2/shipping-order/create";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Content-Type", "application/json");
//        headers.set("Token", ghnApiToken);
//        headers.set("ShopId", shopId);
//
//        Map<String, Object> orderData = new HashMap<>();
//        orderData.put("payment_type_id", 1);
//        orderData.put("note", "Giao nhanh");
//        orderData.put("from_name", "TrendSetter");
//        orderData.put("from_phone", "0386713099");
//        orderData.put("from_address", "Hà Nội");
//        orderData.put("from_district_id", 1442); // ID Quận (GHN yêu cầu)
//
//        // Sử dụng các trường có sẵn trong HoaDon để tạo địa chỉ giao hàng
//        orderData.put("to_name", hoaDon.getNguoiNhan());
//        orderData.put("to_phone", hoaDon.getSoDienThoai());
//        orderData.put("to_address", hoaDon.getSoNha() + ", " + hoaDon.getPhuong() + ", " + hoaDon.getHuyen() + ", " + hoaDon.getThanhPho());
//
//        // Nếu bạn có sẵn ID quận/huyện, có thể lấy từ các trường này (cần bổ sung các thuộc tính cho HoaDon nếu chưa có)
//        orderData.put("to_district_id", hoaDon.getDistrictId()); // ID quận/huyện nơi nhận
//        orderData.put("to_ward_code", hoaDon.getWardCode()); // Mã phường (nếu có)
//
//        orderData.put("cod_amount", hoaDon.getTongTien());
//        orderData.put("weight", 500); // Trọng lượng có thể mặc định nếu không cần thiết
//        orderData.put("length", 30);
//        orderData.put("width", 20);
//        orderData.put("height", 10);
//        orderData.put("service_id", 53320);
//        orderData.put("required_note", "CHOXEMHANGKHONGTHU");
//
//        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(orderData, headers);
//        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
//
//        return response.getBody();
//    }

}
