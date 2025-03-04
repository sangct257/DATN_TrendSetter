package com.example.datn_trendsetter.DTO;

import lombok.*;

import java.util.List;

@Data
public class ProductDetailDTO {
    private Integer sanPhamId;
    private List<Integer> mauSacIds;
    private List<Integer> kichThuocIds;


}
