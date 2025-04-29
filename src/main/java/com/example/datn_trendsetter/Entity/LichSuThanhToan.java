package com.example.datn_trendsetter.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "lich_su_thanh_toan")
public class LichSuThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don", referencedColumnName = "id")
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien", referencedColumnName = "id")
    private NhanVien nhanVien;

    @ManyToOne
    @JoinColumn(name = "id_phuong_thuc_thanh_toan", referencedColumnName = "id")
    private PhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "nguoi_xac_nhan",columnDefinition = "NVARCHAR(255)")
    private String nguoiXacNhan;

    @Column(name = "so_tien_thanh_toan")
    private Float soTienThanhToan;

    @Column(name = "thoi_gian_thanh_toan")
    private LocalDateTime thoiGianThanhToan;

    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(255)")
    private String trangThai;

    @Column(name = "ghi_chu", columnDefinition = "NVARCHAR(255)")
    private String ghiChu;

    @Column(name = "ma_giao_dich", columnDefinition = "NVARCHAR(50)")
    private String maGiaoDich;
}