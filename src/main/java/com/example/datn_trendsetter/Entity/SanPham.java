package com.example.datn_trendsetter.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "san_pham")
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_san_pham",columnDefinition = "NVARCHAR(255)")
    private String maSanPham;

    @Column(name = "ten_san_pham",columnDefinition = "NVARCHAR(255)")
    private String tenSanPham;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "mo_ta",columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_thuong_hieu", referencedColumnName = "id")
    @JsonBackReference
    private ThuongHieu thuongHieu;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_xuat_xu", referencedColumnName = "id")
    private XuatXu xuatXu;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_danh_muc", referencedColumnName = "id")
    private DanhMuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "id_chat_lieu",referencedColumnName = "id")
    private ChatLieu chatLieu;

    @Column(name = "trang_thai",columnDefinition = "NVARCHAR(255)")
    private String trangThai;

    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @Column(name = "ngay_sua")
    private LocalDate ngaySua;

    @Column(name = "nguoi_tao",columnDefinition = "NVARCHAR(255)")
    private String nguoiTao;

    @Column(name = "nguoi_sua",columnDefinition = "NVARCHAR(255)")
    private String nguoiSua;

    @Column(name = "deleted")
    private Boolean deleted;

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("sanPham") // Tránh vòng lặp JSON
    private List<SanPhamChiTiet> chiTietSanPhams;


    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<SanPhamChiTiet> sanPhamChiTiet;

}
