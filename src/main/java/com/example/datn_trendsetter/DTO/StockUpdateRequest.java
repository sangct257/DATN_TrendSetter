package com.example.datn_trendsetter.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockUpdateRequest {
    private Integer idSanPhamChiTiet;
    private Integer soLuong;

    public StockUpdateRequest(Integer idSanPhamChiTiet, Integer soLuong) {
        this.idSanPhamChiTiet = idSanPhamChiTiet;
        this.soLuong = soLuong;
    }
}
