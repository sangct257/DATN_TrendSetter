package com.example.datn_trendsetter.DTO;

import com.example.datn_trendsetter.Entity.DiaChi;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Entity.NhanVien.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails {
    private Integer id;
    private String username;
    private String hoTen;
    private String email;
    private Boolean gioiTinh;
    private LocalDate ngaySinh;
    private String trangThai;
    private String hinhAnh;
    private String userType;

    // Thông tin dành riêng cho nhân viên
    private Role vaiTro;
    private String diaChiNhanVien;

    // Thông tin dành riêng cho khách hàng
    private String soDienThoai;
    private List<DiaChi> diaChis;

    // Phương thức tạo UserDetails từ KhachHang
    public static UserDetails fromKhachHang(KhachHang khachHang) {
        UserDetails details = new UserDetails();
        details.setId(khachHang.getId());
        details.setUsername(khachHang.getUsername());
        details.setHoTen(khachHang.getHoTen());
        details.setEmail(khachHang.getEmail());
        details.setGioiTinh(khachHang.getGioiTinh());
        details.setNgaySinh(khachHang.getNgaySinh());
        details.setTrangThai(khachHang.getTrangThai());
        details.setHinhAnh(khachHang.getHinhAnh());
        details.setUserType("KHACHHANG");
        details.setSoDienThoai(khachHang.getSoDienThoai());
        details.setDiaChis(khachHang.getDiaChis());
        return details;
    }

    // Phương thức tạo UserDetails từ NhanVien
    public static UserDetails fromNhanVien(NhanVien nhanVien) {
        UserDetails details = new UserDetails();
        details.setId(nhanVien.getId());
        details.setUsername(nhanVien.getUsername());
        details.setHoTen(nhanVien.getHoTen());
        details.setEmail(nhanVien.getEmail());
        details.setGioiTinh(nhanVien.getGioiTinh());
        details.setNgaySinh(nhanVien.getNgaySinh());
        details.setTrangThai(nhanVien.getTrangThai());
        details.setHinhAnh(nhanVien.getHinhAnh());
        details.setUserType("NHANVIEN");
        details.setVaiTro(nhanVien.getVaiTro());
        details.setDiaChiNhanVien(nhanVien.getDiaChi());
        return details;
    }

    // Phương thức kiểm tra có phải admin không
    public boolean isAdmin() {
        return "NHANVIEN".equals(userType) && vaiTro == Role.ADMIN;
    }

    // Phương thức kiểm tra có phải nhân viên không
    public boolean isNhanVien() {
        return "NHANVIEN".equals(userType) && vaiTro == Role.NHANVIEN;
    }

    // Phương thức kiểm tra có phải khách hàng không
    public boolean isKhachHang() {
        return "KHACHHANG".equals(userType);
    }
}