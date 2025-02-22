package com.example.datn_trendsetter.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lich_su_hoa_don")
public class LichSuHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don", referencedColumnName = "id")
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang", referencedColumnName = "id")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien", referencedColumnName = "id")
    private NhanVien nhanVien;

    @Column(name = "hanh_dong", columnDefinition = "NVARCHAR(255)")
    private String hanhDong;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @Column(name = "nguoi_tao", columnDefinition = "NVARCHAR(255)")
    private String nguoiTao;

    @Column(name = "nguoi_sua", columnDefinition = "NVARCHAR(255)")
    private String nguoiSua;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "ghi_chu", columnDefinition = "NVARCHAR(255)")
    private String ghiChu;

    // Constructor tùy chỉnh khớp với cách gọi trong createHoaDon()
    public LichSuHoaDon(HoaDon hoaDon, KhachHang khachHang, NhanVien nhanVien, String hanhDong, LocalDateTime ngayTao, String nguoiTao, Boolean deleted, String ghiChu) {
        this.hoaDon = hoaDon;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
        this.hanhDong = hanhDong; // Cần khởi tạo giá trị hành động
        this.ngayTao = ngayTao;
        this.ngaySua = null; // Mặc định null khi mới tạo
        this.nguoiTao = nguoiTao;
        this.nguoiSua = null;
        this.deleted = deleted;
        this.ghiChu = ghiChu;
    }
}
