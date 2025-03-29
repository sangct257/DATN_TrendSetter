package com.example.datn_trendsetter.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonDTO {
    private Integer id;
    private Integer idKhachHang;
    private Integer idNhanVien;
    private Integer idPhuongThucThanhToan;
    private Integer idPhieuGiamGia;

    private String maHoaDon;
    private String maGiaoDich;
    private Float tongTien;
    private Float phiShip;

    private String nguoiNhan;
    private String soDienThoai;
    private String email;
    private String diaChiCuThe;
    private String huyen;
    private String phuong;
    private String thanhPho;

    private String ghiChu;
    private String trangThai;
    private String loaiHoaDon;
    private String loaiGiaoDich;
    private String qrImage;

    private LocalDateTime ngayTao;
    private LocalDateTime ngaySua;
    private LocalDate thoiGianNhanDuKien;
    private String nguoiTao;
    private String nguoiSua;

    private int tongSanPham;
    private Float soTienDaThanhToan;

    private List<HoaDonChiTietDTO> hoaDonChiTiet;
}