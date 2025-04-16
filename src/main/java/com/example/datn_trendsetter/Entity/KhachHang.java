package com.example.datn_trendsetter.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "reset_token")
    private String resetToken;
    @Column(name = "ho_ten",columnDefinition = "NVARCHAR(255)")
    private String hoTen;

    @Column(name = "username",columnDefinition = "NVARCHAR(255)")
    private String username;

    @Column(name = "password",columnDefinition = "NVARCHAR(255)")
    private String password;

    @Column(name = "so_dien_thoai",columnDefinition = "NVARCHAR(255)")
    private String soDienThoai;

    @Column(name = "email",columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "trang_thai",columnDefinition = "NVARCHAR(255)")
    private String trangThai;

    @Column(name = "hinh_anh",columnDefinition = "NVARCHAR(255)")
    private String hinhAnh;

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    private List<DiaChi> diaChis = new ArrayList<>();

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    private List<HoaDon> hoaDons = new ArrayList<>();
}
