package com.example.datn_trendsetter.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonChiTietDTO {
    private Integer idSanPhamChiTiet;
    private Integer soLuong;
    private Float gia;
    private Float thanhTien;
}

