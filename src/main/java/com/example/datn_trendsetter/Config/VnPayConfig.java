package com.example.datn_trendsetter.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VnPayConfig {
    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.secretKey}")
    private String secretKey; // 🔥 Đổi từ hashSecret thành secretKey

    @Value("${vnpay.url}")
    private String vnpUrl;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    public String getTmnCode() {
        return tmnCode;
    }

    public String getSecretKey() { // 🔥 Đổi từ getHashSecret() thành getSecretKey()
        return secretKey;
    }

    public String getVnpUrl() {
        return vnpUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }
}
