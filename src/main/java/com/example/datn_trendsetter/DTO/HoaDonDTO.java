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

    // Constructor tuỳ chỉnh
    public HoaDonDTO(Integer id, Integer idKhachHang, Integer idNhanVien, String maHoaDon, String maGiaoDich,
                     Float tongTien, Float phiShip, String nguoiNhan, String soDienThoai, String email,
                     String diaChiCuThe, String huyen, String phuong, String thanhPho, String ghiChu,
                     String trangThai, String loaiHoaDon, String loaiGiaoDich, String qrImage,
                     LocalDateTime ngayTao, LocalDateTime ngaySua, LocalDate thoiGianNhanDuKien,
                     String nguoiTao, String nguoiSua, int tongSanPham, Float soTienDaThanhToan,
                     List<HoaDonChiTietDTO> hoaDonChiTiet) {
        this.id = id;
        this.idKhachHang = idKhachHang;
        this.idNhanVien = idNhanVien;
        this.maHoaDon = maHoaDon;
        this.maGiaoDich = maGiaoDich;
        this.tongTien = tongTien;
        this.phiShip = phiShip;
        this.nguoiNhan = nguoiNhan;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChiCuThe = diaChiCuThe;
        this.huyen = huyen;
        this.phuong = phuong;
        this.thanhPho = thanhPho;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
        this.loaiHoaDon = loaiHoaDon;
        this.loaiGiaoDich = loaiGiaoDich;
        this.qrImage = qrImage;
        this.ngayTao = ngayTao;
        this.ngaySua = ngaySua;
        this.thoiGianNhanDuKien = thoiGianNhanDuKien;
        this.nguoiTao = nguoiTao;
        this.nguoiSua = nguoiSua;
        this.tongSanPham = tongSanPham;
        this.soTienDaThanhToan = soTienDaThanhToan;
        this.hoaDonChiTiet = hoaDonChiTiet;
    }

    public HoaDonDTO(Integer id, String maHoaDon,
                     Integer idKhachHang,
                     String nguoiTao,
                     String loaiHoaDon,
                     LocalDateTime ngayTao,
                     Float tongTien,
                     Float phiShip) {
        this.id = id;
        this.idKhachHang = idKhachHang;
        this.maHoaDon = maHoaDon;
        this.tongTien = tongTien;
        this.phiShip = phiShip;
        this.loaiHoaDon = loaiHoaDon;
        this.ngayTao = ngayTao;
        this.nguoiTao = nguoiTao;
    }
}
