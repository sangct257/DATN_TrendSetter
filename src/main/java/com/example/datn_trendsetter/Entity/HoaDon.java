package com.example.datn_trendsetter.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "ma_hoa_don")
    private String maHoaDon;

    @Column(name = "tong_tien")
    private Double tongTien;

    @Column(name = "nguoi_nhan")
    private String nguoiNhan;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "so_nha")
    private Integer soNha;

    @Column(name = "huyen")
    private String huyen;

    @Column(name = "phuong")
    private String phuong;

    @Column(name = "thanh_pho")
    private String thanhPho;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @Column(name = "nguoi_tao")
    private String nguoiTao;

    @Column(name = "nguoi_sua")
    private String nguoiSua;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "qr_image")
    private String qrImage;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "phi_ship")
    private Double phiShip ;

    @Column(name = "loai_hoa_don")
    private String loaiHoaDon;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HoaDonChiTiet> hoaDonChiTiet = new ArrayList<>();

    @OneToMany(mappedBy = "hoaDon",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LichSuHoaDon> lichSuHoaDons = new ArrayList<>();

    @Transient // Không lưu vào cơ sở dữ liệu
    private int tongSanPham;

}
