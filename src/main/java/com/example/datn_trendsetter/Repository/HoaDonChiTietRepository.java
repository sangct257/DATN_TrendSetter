package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.HoaDonChiTiet;
import com.example.datn_trendsetter.Entity.SanPhamChiTiet;
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
            "AND YEAR(hdct.hoaDon.ngayTao) = YEAR(CURRENT_DATE) " +
            "AND hdct.hoaDon.trangThai = :trangThai")
    int getTongSanPhamBanTrongThang(@Param("trangThai") String trangThai);

    @Query("SELECT ha.urlHinhAnh, " +
            "CONCAT(sp.tenSanPham, ' - ', ms.tenMauSac, ' - ', kt.tenKichThuoc) AS productInfo, " +
            "spct.gia, spct.soLuong, SUM(hdct.soLuong) AS totalQuantity, " +
            "sp.trangThai " +    // <<< THÊM dòng này
            "FROM HoaDonChiTiet hdct " +
            "JOIN SanPhamChiTiet spct ON hdct.sanPhamChiTiet.id = spct.id " +
            "JOIN SanPham sp ON spct.sanPham.id = sp.id " +
            "JOIN MauSac ms ON spct.mauSac.id = ms.id " +
            "JOIN KichThuoc kt ON spct.kichThuoc.id = kt.id " +
            "JOIN HinhAnh ha ON spct.id = ha.sanPhamChiTiet.id " +
            "JOIN HoaDon hd ON hdct.hoaDon.id = hd.id " +
            "WHERE MONTH(hd.ngayTao) = MONTH(CURRENT_DATE) " +
            "  AND YEAR(hd.ngayTao) = YEAR(CURRENT_DATE) " +
            "  AND hd.trangThai = :trangThai " +
            "GROUP BY ha.urlHinhAnh, sp.tenSanPham, ms.tenMauSac, kt.tenKichThuoc, spct.gia, spct.soLuong, sp.trangThai " + // <<< Thêm sp.trangThai vào GROUP BY
            "ORDER BY totalQuantity DESC")
    List<Object[]> getTotalSoldByProductInMonth(@Param("trangThai") String trangThai);



    @Query("SELECT YEAR(h.hoaDon.ngayTao), MONTH(h.hoaDon.ngayTao), DAY(h.hoaDon.ngayTao), SUM(h.soLuong) " +
            "FROM HoaDonChiTiet h " +
            "WHERE h.hoaDon.trangThai IN :trangThaiHoaDon " +
            "GROUP BY YEAR(h.hoaDon.ngayTao), MONTH(h.hoaDon.ngayTao), DAY(h.hoaDon.ngayTao) " +
            "ORDER BY YEAR(h.hoaDon.ngayTao), MONTH(h.hoaDon.ngayTao), DAY(h.hoaDon.ngayTao)")
    List<Object[]> getTotalProductsByDateMonthYear(
            @Param("trangThaiHoaDon") List<String> trangThaiHoaDon);


    @Query("SELECT COUNT(hdc.soLuong) FROM HoaDonChiTiet hdc WHERE hdc.hoaDon.ngayTao BETWEEN :startDate AND :endDate")
    Integer countSanPhamChiTietByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    boolean existsBySanPhamChiTiet(SanPhamChiTiet sanPhamChiTiet);

    @Query("SELECT COALESCE(SUM(hdct.thanhTien), 0) FROM HoaDonChiTiet hdct WHERE hdct.hoaDon.id = :hoaDonId")
    Float getTongThanhTienByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

}
