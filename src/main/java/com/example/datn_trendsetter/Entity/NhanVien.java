package com.example.datn_trendsetter.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nhan_vien")
public class NhanVien {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ho_ten",columnDefinition = "NVARCHAR(255)")
    private String hoTen;

    @Column(name = "username",columnDefinition = "NVARCHAR(255)")
    private String username;

    @Column(name = "password",columnDefinition = "NVARCHAR(255)")
    private String password;

    @Column(name = "email",columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "dia_chi",columnDefinition = "NVARCHAR(255)")
    private String diaChi;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "trang_thai",columnDefinition = "NVARCHAR(255)")
    private String trangThai;

    @Column(name = "vai_tro",columnDefinition = "NVARCHAR(255)")
    private String vaiTro;

    @Column(name = "hinh_anh",columnDefinition = "NVARCHAR(255)")
    private String hinhAnh;
}
