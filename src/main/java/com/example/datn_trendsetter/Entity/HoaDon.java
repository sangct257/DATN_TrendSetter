package com.example.datn_trendsetter.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "hoa_don")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang",referencedColumnName = "id")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien",referencedColumnName = "id")
    private NhanVien nhanVien;

    @ManyToOne
    @JoinColumn(name = "id_phieu_giam_gia",referencedColumnName = "id")
    private PhieuGiamGia phieuGiamGia;

    @ManyToOne
    @JoinColumn(name = "id_phuong_thuc_thanh_toan",referencedColumnName = "id")
    private PhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "ma_hoa_don",columnDefinition = "NVARCHAR(255)")
    private String maHoaDon;

    @Column(name = "tong_tien")
    private Float tongTien;

    @Column(name = "nguoi_nhan",columnDefinition = "NVARCHAR(255)")
    private String nguoiNhan;

    @Column(name = "so_dien_thoai",columnDefinition = "NVARCHAR(255)")
    private String soDienThoai;

    @Column(name = "email",columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "so_nha")
    private Integer soNha;

    @Column(name = "ten_duong",columnDefinition = "NVARCHAR(255)")
    private String tenDuong;

    @Column(name = "huyen",columnDefinition = "NVARCHAR(255)")
    private String huyen;

    @Column(name = "phuong",columnDefinition = "NVARCHAR(255)")
    private String phuong;

    @Column(name = "thanh_pho",columnDefinition = "NVARCHAR(255)")
    private String thanhPho;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @Column(name = "thoi_gian_nhan_du_kien")
    private LocalDate thoiGianNhanDuKien;

    @Column(name = "nguoi_tao",columnDefinition = "NVARCHAR(255)")
    private String nguoiTao;

    @Column(name = "nguoi_sua",columnDefinition = "NVARCHAR(255)")
    private String nguoiSua;

    @Column(name = "trang_thai",columnDefinition = "NVARCHAR(255)")
    private String trangThai;

    @Column(name = "qr_image",columnDefinition = "NVARCHAR(255)")
    private String qrImage;

    @Column(name = "ghi_chu",columnDefinition = "NVARCHAR(255)")
    private String ghiChu;

    @Column(name = "phi_ship")
    private Float phiShip ;

    @Column(name = "loai_hoa_don",columnDefinition = "NVARCHAR(255)")
    private String loaiHoaDon;

    @Column(name = "loai_giao_dich",columnDefinition = "NVARCHAR(255)")
    private String loaiGiaoDich;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HoaDonChiTiet> hoaDonChiTiet = new ArrayList<>();

    @OneToMany(mappedBy = "hoaDon",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LichSuHoaDon> lichSuHoaDons = new ArrayList<>();

    @Transient // Không lưu vào cơ sở dữ liệu
    private int tongSanPham;

}
