package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Integer> {
    List<LichSuThanhToan> findByHoaDonId(Integer hoaDonId);

    @Query("SELECT COALESCE(SUM(l.soTienThanhToan), 0) FROM LichSuThanhToan l WHERE l.hoaDon.id = :hoaDonId")
    float sumSoTienThanhToanByHoaDon(@Param("hoaDonId") Integer hoaDonId);

    @Query("SELECT SUM(l.soTienThanhToan) FROM LichSuThanhToan l WHERE l.hoaDon.id = :hoaDonId")
    Optional<Float> sumSoTienThanhToanByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    @Query("SELECT YEAR(lst.thoiGianThanhToan), MONTH(lst.thoiGianThanhToan), DAY(lst.thoiGianThanhToan), SUM(lst.soTienThanhToan) " +
            "FROM LichSuThanhToan lst " +
            "JOIN lst.hoaDon hd " +
            "WHERE hd.trangThai IN :trangThaiHoaDon " +
            "GROUP BY YEAR(lst.thoiGianThanhToan), MONTH(lst.thoiGianThanhToan), DAY(lst.thoiGianThanhToan) " +
            "ORDER BY YEAR(lst.thoiGianThanhToan), MONTH(lst.thoiGianThanhToan), DAY(lst.thoiGianThanhToan)")
    List<Object[]> getTotalRevenueByDateMonthYear(
            @Param("trangThaiHoaDon") List<String> trangThaiHoaDon
    );



}
