package com.example.datn_trendsetter.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRequest {
    private Integer hoaDonId;
    private String nguoiNhan;
    private String soDienThoai;
    private Integer soNha;
    private String tenDuong;
    private String phuong;
    private String huyen;
    private String thanhPho;
    private String ghiChu;
}
