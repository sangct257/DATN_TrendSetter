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
    private String trangThai;
}
