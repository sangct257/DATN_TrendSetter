package com.example.datn_trendsetter.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangDTO {
    private Integer id;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private Boolean gioiTinh;
}
