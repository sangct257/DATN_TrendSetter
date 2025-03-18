package com.example.datn_trendsetter.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhieuGiamGiaDTO {
    private Integer id;
    private String maPhieuGiamGia;
    private Boolean deleted;
    private Float dieuKien;
    private String moTa;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String ngayBatDau;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String ngayKetThuc;
    private String tenPhieuGiamGia;
    private String donViTinh;
    private String trangThai;
    private Float giaTriGiam;
    private Integer soLuotSuDung;
}
