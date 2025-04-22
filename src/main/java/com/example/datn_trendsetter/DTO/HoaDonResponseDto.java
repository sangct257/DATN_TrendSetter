package com.example.datn_trendsetter.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HoaDonResponseDto {

    private Integer id;
    private String maHoaDon;
    private String khachHang;
    private String nguoiTao;
    private String loaiHoaDon;
    private LocalDateTime ngayTao;
    private Float giaTriGiam;
    private Float tongTien;

    public HoaDonResponseDto(Integer id, String maHoaDon, String khachHang, String nguoiTao, String loaiHoaDon,
                             LocalDateTime ngayTao, Float giaTriGiam, Float tongTien) {
        this.id = id;
        this.maHoaDon = maHoaDon;
        this.khachHang = khachHang;
        this.nguoiTao = nguoiTao;
        this.loaiHoaDon = loaiHoaDon;
        this.ngayTao = ngayTao;
        this.giaTriGiam = giaTriGiam;
        this.tongTien = tongTien;
    }

    // Getters and Setters
}

