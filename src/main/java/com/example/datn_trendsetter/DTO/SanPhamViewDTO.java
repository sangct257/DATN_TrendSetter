package com.example.datn_trendsetter.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SanPhamViewDTO {
    private Integer id;
    private String tenSanPham;
    private Float gia;
    private String urlHinhAnh;

    public SanPhamViewDTO(Integer id, String tenSanPham, Float gia, String urlHinhAnh) {
        this.id = id;
        this.tenSanPham = tenSanPham;
        this.gia = (gia == null) ? 0.0f : gia;  // Đảm bảo không có null
        this.urlHinhAnh = (urlHinhAnh == null) ? "" : urlHinhAnh;  // Đảm bảo không có null
    }
}
