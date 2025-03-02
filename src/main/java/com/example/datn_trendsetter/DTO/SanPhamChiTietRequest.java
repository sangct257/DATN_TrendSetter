package com.example.datn_trendsetter.DTO;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;

@Data
public class SanPhamChiTietRequest {
    private Integer id;
    private Integer soLuong;
    private Float gia;
}
