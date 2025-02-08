package com.example.datn_trendsetter.DTO;

import lombok.Data;

@Data
public class ShippingUpdateRequest {
    private Integer hoaDonId;
    private Integer khachHangId;
    private Integer soNha;
    private String phuong;
    private String huyen;
    private String thanhPho;
}
