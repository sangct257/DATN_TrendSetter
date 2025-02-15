package com.example.datn_trendsetter.DTO;

import lombok.Data;

@Data
public class SanPhamChiTietDTO {
    private String tenSanPham;
    private String tenMauSac;
    private String tenKichThuoc;

    // Constructor cho phép ánh xạ dữ liệu từ query
    public SanPhamChiTietDTO(String tenSanPham, String tenMauSac, String tenKichThuoc) {
        this.tenSanPham = tenSanPham;
        this.tenMauSac = tenMauSac;
        this.tenKichThuoc = tenKichThuoc;
    }
}

