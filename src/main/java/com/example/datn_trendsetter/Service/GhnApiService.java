package com.example.datn_trendsetter.Service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Service
public class GhnApiService {
    @Value("${ghn.api.token}")
    private String ghnApiToken;

    @Value("${ghn.client.id}")
    private String ghnClientId;

    @Value("${ghn.shop.id}")
    private String ghnShopId;

    private static final String BASE_URL = "https://online-gateway.ghn.vn/shiip/public-api/";

    private final RestTemplate restTemplate = new RestTemplate();

    // Lấy danh sách Thành Phố/Tỉnh
    public ResponseEntity<String> getProvinces() {
        String url = BASE_URL + "master-data/province";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnApiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    // Lấy danh sách Quận/Huyện theo Tỉnh
    public ResponseEntity<String> getDistricts(int provinceId) {
        String url = BASE_URL + "master-data/district?province_id=" + provinceId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnApiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    // Lấy danh sách Phường/Xã theo Quận
    public ResponseEntity<String> getWards(int districtId) {
        String url = BASE_URL + "master-data/ward?district_id=" + districtId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnApiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    public ResponseEntity<String> getShopAddresses() {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/v2/shop/all";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnApiToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    public String getShopAddressByShopId(String shopId) {
        ResponseEntity<String> response = getShopAddresses();
        System.out.println("GHN API Response: " + response.getBody()); // Ghi log dữ liệu trả về

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                Object data = jsonResponse.get("data");

                if (data instanceof JSONArray) {
                    JSONArray dataArray = (JSONArray) data;
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject shop = dataArray.getJSONObject(i);
                        System.out.println("Checking Shop: " + shop.toString()); // Log từng shop
                        if (shop.getString("shop_id").equals(shopId)) {
                            return shop.getString("address_id");
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public ResponseEntity<String> calculateShippingFee(int toDistrictId, String toWardCode, int weight) {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnApiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("from_district_id", 1582); // Quận của shop
        requestBody.put("service_id", 53320); // Loại dịch vụ (GHN cung cấp)
        requestBody.put("to_district_id", toDistrictId); // Quận/Huyện khách hàng
        requestBody.put("to_ward_code", toWardCode); // Phường/Xã khách hàng
        requestBody.put("height", 10); // Chiều cao (cm)
        requestBody.put("length", 10); // Chiều dài (cm)
        requestBody.put("width", 10); // Chiều rộng (cm)
        requestBody.put("weight", weight); // Khối lượng (gram)

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }


}
