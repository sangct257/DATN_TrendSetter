package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Config.VnPayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class VnPayService {
    @Autowired
    private VnPayConfig vnPayConfig;

    public String createPaymentUrl(String orderId, int amount, String ipAddr) {
        System.out.println("📌 Bắt đầu tạo URL thanh toán");

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", orderId);
        vnp_Params.put("vnp_OrderInfo", "Thanh toán đơn hàng #" + orderId);
        vnp_Params.put("vnp_OrderType", "other"); // ⚠️ Bổ sung tránh lỗi định dạng
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", ipAddr);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(new Date()));

        // Xóa `vnp_SecureHash` trước khi ký
        vnp_Params.remove("vnp_SecureHash");

        // Tạo chuỗi ký
        String signData = buildQueryString(vnp_Params);
        System.out.println("🔹 Chuỗi ký trước khi hash: " + signData);

        // Tạo chữ ký
        String secureHash = hmacSHA512(vnPayConfig.getSecretKey(), signData);
        System.out.println("🔹 Secure Hash: " + secureHash);

        // Thêm Secure Hash vào danh sách tham số
        vnp_Params.put("vnp_SecureHash", secureHash);

        // Tạo URL thanh toán
        String paymentUrl = vnPayConfig.getVnpUrl() + "?" + buildQueryString(vnp_Params);
        System.out.println("✅ URL thanh toán: " + paymentUrl);

        return paymentUrl;
    }



    private String buildQueryString(Map<String, String> params) {
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            queryString.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)) // ⚠️ Dùng UTF_8
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)) // ⚠️ Dùng UTF_8
                    .append("&");
        }
        return queryString.substring(0, queryString.length() - 1);
    }
    
    private String hmacSHA512(String key, String data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa HMAC-SHA512", e);
        }
    }


}
