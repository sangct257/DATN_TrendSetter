package com.example.datn_trendsetter.DTO;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class KhachHangDTO {
    private Integer id;
    private String hoTen;
    private String username;
    private String soDienThoai;
    private String email;
    private Boolean gioiTinh;
    private LocalDate ngaySinh;
    private String trangThai;
    private String hinhAnh;

}
