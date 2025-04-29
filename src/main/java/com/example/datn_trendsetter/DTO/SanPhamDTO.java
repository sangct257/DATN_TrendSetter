package com.example.datn_trendsetter.DTO;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SanPhamDTO {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private Integer soLuong;
    private String moTa;
    private Integer thuongHieuId;
    private Integer xuatXuId;
    private Integer danhMucId;
    private Integer chatLieuId;
    private String trangThai;
    private LocalDate ngayTao;
    private LocalDate ngaySua;
    private String nguoiTao;
    private String nguoiSua;
    private Boolean deleted;
    private List<SanPhamChiTietDTO> sanPhamChiTiet;

}
