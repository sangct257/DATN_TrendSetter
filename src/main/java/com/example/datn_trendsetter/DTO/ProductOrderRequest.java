package com.example.datn_trendsetter.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductOrderRequest {
    private Integer sanPhamChiTietId;
    private Integer hoaDonId;
    private Integer soLuong;
    private Integer hoaDonChiTietId;

    // Getter và Setter
    public Integer getHoaDonChiTietId() {
        return hoaDonChiTietId;
    }

    public void setHoaDonChiTietId(Integer hoaDonChiTietId) {
        this.hoaDonChiTietId = hoaDonChiTietId;
    }

    public Integer getSanPhamChiTietId() {
        return sanPhamChiTietId;
    }

    public void setSanPhamChiTietId(Integer sanPhamChiTietId) {
        this.sanPhamChiTietId = sanPhamChiTietId;
    }

    public Integer getHoaDonId() {
        return hoaDonId;
    }

    public void setHoaDonId(Integer hoaDonId) {
        this.hoaDonId = hoaDonId;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }
}
