package com.example.datn_trendsetter.DTO;

import lombok.Data;

import java.util.List;

@Data
public class DanhMucDTO {

    private Integer id;
    private String maDanhMuc;
    private String tenDanhMuc;
    private List<SanPhamViewDTO> sanPhamList; // Gồm những sản phẩm dạng view nhỏ gọn

}
