package com.example.datn_trendsetter.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Data
@NoArgsConstructor
public class SanPhamChiTietViewDTO {
    private Integer idSanPhamChiTiet;
    private String tenSanPham;
    private String tenMauSac;
    private Float gia;
    private String moTa;
    private String trangThai;
    private List<String> sizes = new ArrayList<>(); // Danh sách kích thước
    private Set<String> hinhAnh = new HashSet<>(); // Sử dụng Set để tránh trùng lặp
    private Map<String, Integer> soLuongTheoSize = new HashMap<>(); // Số lượng theo size

    // Constructor đầy đủ dữ liệu từ @Query
    public SanPhamChiTietViewDTO(Integer idSanPhamChiTiet, String tenSanPham, Float gia, String moTa,
                                 String tenKichThuoc, String tenMauSac, String hinhAnh, Integer soLuong) {
        this.idSanPhamChiTiet = idSanPhamChiTiet;
        this.tenSanPham = tenSanPham;
        this.gia = gia;
        this.moTa = moTa;
        this.tenMauSac = tenMauSac;

        // Thêm size và số lượng ngay khi khởi tạo
        if (tenKichThuoc != null) {
            this.sizes.add(tenKichThuoc);
            this.soLuongTheoSize.put(tenKichThuoc, soLuong);
        }

        // Thêm hình ảnh nếu có
        if (hinhAnh != null) {
            this.hinhAnh.add(hinhAnh);
        }
    }
}
