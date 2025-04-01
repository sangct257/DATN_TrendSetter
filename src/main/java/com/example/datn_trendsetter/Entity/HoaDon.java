package com.example.datn_trendsetter.Entity;

import com.example.datn_trendsetter.Repository.HoaDonChiTietRepository;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpHeaders;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
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

    @Column(name = "dia_chi_cu_the",columnDefinition = "NVARCHAR(255)")
    private String diaChiCuThe;

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
    private List<LichSuHoaDon> lichSuHoaDon = new ArrayList<>();

    @Transient // Không lưu vào cơ sở dữ liệu
    private int tongSanPham;

    @Transient
    private Float soTienDaThanhToan;


    @PreUpdate
    @PrePersist
    public void updatePhiShip() {
        if ("Tại Quầy".equals(this.loaiHoaDon)) {
            this.phiShip = 0F; // Tại quầy không có phí ship
            this.tinhTongTienHoaDon();
        } else if ("Giao Hàng".equals(this.loaiHoaDon)) {
            this.phiShip = tinhPhiShip(); // Chỉ tính phí ship nếu là Giao Hàng
            this.tinhTongTienHoaDon();
        }
        // "Online" không cần tính phí ship
    }

    private float tinhPhiShip() {
        if (this.nguoiNhan != null && !this.nguoiNhan.isEmpty()
                && this.soDienThoai != null && !this.soDienThoai.isEmpty()
                && this.diaChiCuThe != null && !this.diaChiCuThe.isEmpty()
                && this.huyen != null && !this.huyen.isEmpty()
                && this.phuong != null && !this.phuong.isEmpty()
                && this.thanhPho != null && !this.thanhPho.isEmpty()) {
            return 30000.0F; // Nếu có đầy đủ thông tin → tính phí ship
        }
        return 0F; // Nếu thiếu thông tin → miễn phí ship
    }

    private void tinhTongTienHoaDon() {
        float tongTienSanPham = (float) hoaDonChiTiet.stream()
                .mapToDouble(HoaDonChiTiet::getThanhTien)
                .sum();

        float giaTriGiam = 0F;

        if (phieuGiamGia != null && phieuGiamGia.getDieuKien() != null) {
            if (tongTienSanPham >= phieuGiamGia.getDieuKien()) {
                giaTriGiam = Objects.requireNonNullElse(phieuGiamGia.getGiaTriGiam(), 0F);
            } else {
                this.phieuGiamGia = null;
            }
        }

        float phiShip = Objects.requireNonNullElse(this.phiShip, 0F);

        // **Gán giá trị tổng tiền vào thuộc tính `tongTien`**
        this.tongTien = Math.max(0, tongTienSanPham + phiShip - giaTriGiam);
    }




}
