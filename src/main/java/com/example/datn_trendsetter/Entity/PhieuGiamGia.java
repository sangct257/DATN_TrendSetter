package com.example.datn_trendsetter.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_chuong_trinh", columnDefinition = "NVARCHAR(255)")
    private String tenChuongTrinh;

    @Column(name = "gia_tri")
    private Float giaTri;

    @Column(name = "dieu_kien")
    private Float dieuKien;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(255)")
    private String moTa;

    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(255)")
    private String trangThai;

    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @Column(name = "ngay_sua")
    private LocalDate ngaySua;

    @Column(name = "nguoi_tao", columnDefinition = "NVARCHAR(255)")
    private String nguoiTao;

    @Column(name = "nguoi_sua", columnDefinition = "NVARCHAR(255)")
    private String nguoiSua;

    @Column(name = "deleted")
    private Boolean deleted;

    // Trường bổ sung
    @Column(name = "loai_ap_dung", columnDefinition = "NVARCHAR(50)")
    private String loaiApDung; // "PERCENTAGE" hoặc "FIXED_AMOUNT"

    @Column(name = "so_luot_su_dung")
    private Integer soLuotSuDung; // Số lượt có thể sử dụng

    @Column(name = "so_luot_da_dung")
    private Integer soLuotDaDung; // Số lượt đã sử dụng

    @Column(name = "is_active")
    private Boolean isActive; // Trạng thái hoạt động
}
