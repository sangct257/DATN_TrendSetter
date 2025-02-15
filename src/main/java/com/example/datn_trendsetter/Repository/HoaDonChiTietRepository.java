package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.DTO.ProductInfoDTO;
import com.example.datn_trendsetter.Entity.HoaDonChiTiet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {
    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);

    Page<HoaDonChiTiet> findByHoaDon_Id(Integer hoaDonId,Pageable pageable);

    Optional<HoaDonChiTiet> findByHoaDonIdAndSanPhamChiTietId(Integer hoaDonId, Integer sanPhamChiTietId);

    @Query("SELECT COALESCE(SUM(hdct.soLuong), 0) FROM HoaDonChiTiet hdct " +
            "WHERE MONTH(hdct.hoaDon.ngayTao) = MONTH(CURRENT_DATE) " +
            "AND YEAR(hdct.hoaDon.ngayTao) = YEAR(CURRENT_DATE)")
    int getTongSanPhamBanTrongThang();

    @Query("SELECT ha.urlHinhAnh, " +
            "CONCAT(sp.tenSanPham, ' - ', ms.tenMauSac, ' - ', kt.tenKichThuoc) AS productInfo, " +
            "spct.gia, spct.soLuong, SUM(hdct.soLuong) AS totalQuantity " +
            "FROM HoaDonChiTiet hdct " +
            "JOIN SanPhamChiTiet spct ON hdct.sanPhamChiTiet.id = spct.id " +
            "JOIN SanPham sp ON spct.sanPham.id = sp.id " +
            "JOIN MauSac ms ON spct.mauSac.id = ms.id " +
            "JOIN KichThuoc kt ON spct.kichThuoc.id = kt.id " +
            "JOIN HinhAnh ha ON spct.id = ha.sanPhamChiTiet.id " +
            "JOIN HoaDon hd ON hdct.hoaDon.id = hd.id " +
            "WHERE MONTH(hd.ngayTao) = MONTH(CURRENT_DATE) " +
            "  AND YEAR(hd.ngayTao) = YEAR(CURRENT_DATE) " +
            "GROUP BY ha.urlHinhAnh, sp.tenSanPham, ms.tenMauSac, kt.tenKichThuoc, spct.gia, spct.soLuong " +
            "ORDER BY totalQuantity DESC ")
    Page<Object[]> getTotalSoldByProductInMonth(Pageable pageable);


    @Query("SELECT YEAR(h.hoaDon.ngayTao), MONTH(h.hoaDon.ngayTao), DAY(h.hoaDon.ngayTao), SUM(h.soLuong) " +
            "FROM HoaDonChiTiet h " +
            "GROUP BY YEAR(h.hoaDon.ngayTao), MONTH(h.hoaDon.ngayTao), DAY(h.hoaDon.ngayTao) " +
            "ORDER BY YEAR(h.hoaDon.ngayTao), MONTH(h.hoaDon.ngayTao), DAY(h.hoaDon.ngayTao)")
    List<Object[]> getTotalProductsByDateMonthYear();


    @Query("SELECT COUNT(hdc.soLuong) FROM HoaDonChiTiet hdc WHERE hdc.hoaDon.ngayTao BETWEEN :startDate AND :endDate")
    Integer countSanPhamChiTietByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
