package com.example.datn_trendsetter.DTO;

import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.NhanVien;

import java.util.List;

public class AuthResponse {
    private Object userEntity; // NhanVien hoặc KhachHang
    private UserDetails userDetails;
    private String redirectUrl;
    private List<String> roles;

    public AuthResponse(Object userEntity, UserDetails userDetails, String redirectUrl, List<String> roles) {
        this.userEntity = userEntity;
        this.userDetails = userDetails;
        this.redirectUrl = redirectUrl;
        this.roles = roles;
    }

    // Phương thức generic để lấy user entity
    public <T> T getUserEntity(Class<T> clazz) {
        if (clazz.isInstance(userEntity)) {
            return clazz.cast(userEntity);
        }
        return null;
    }

    // Phương thức riêng cho từng loại user
    public NhanVien getNhanVien() {
        return (userEntity instanceof NhanVien) ? (NhanVien) userEntity : null;
    }

    public KhachHang getKhachHang() {
        return (userEntity instanceof KhachHang) ? (KhachHang) userEntity : null;
    }

    // Getter methods
    public UserDetails getUserDetails() {
        return userDetails;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public List<String> getRoles() {
        return roles;
    }
}