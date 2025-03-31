package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    // Lấy danh sách hóa đơn theo trạng thái
    List<HoaDon> findByTrangThai(String trangThai);

    @Query("select hd from HoaDon hd order by hd.id desc")
    List<HoaDon> getAllHoaDon();

    boolean existsByMaHoaDon(String maHoaDon);

    @Query("SELECT COALESCE(SUM(h.tongTien - COALESCE(h.phiShip, 0) + COALESCE(h.phieuGiamGia.giaTriGiam, 0)), 0) " +
            "FROM HoaDon h " +
            "LEFT JOIN h.phieuGiamGia pg " +
            "WHERE YEAR(h.ngayTao) = YEAR(:now) AND h.trangThai = :trangThai")
    Float getDoanhSoNamNay(@Param("now") LocalDateTime now, @Param("trangThai") String trangThai);

    @Query("SELECT COALESCE(SUM(h.tongTien - COALESCE(h.phiShip, 0) + COALESCE(h.phieuGiamGia.giaTriGiam, 0)), 0) " +
            "FROM HoaDon h " +
            "LEFT JOIN h.phieuGiamGia pg " +
            "WHERE MONTH(h.ngayTao) = MONTH(:now) AND YEAR(h.ngayTao) = YEAR(:now) AND h.trangThai = :trangThai")
    Float getDoanhSoThangNay(@Param("now") LocalDateTime now, @Param("trangThai") String trangThai);

    @Query("SELECT COALESCE(SUM(h.tongTien - COALESCE(h.phiShip, 0) + COALESCE(h.phieuGiamGia.giaTriGiam, 0)), 0) " +
            "FROM HoaDon h " +
            "LEFT JOIN h.phieuGiamGia pg " +
            "WHERE DAY(h.ngayTao) = DAY(:now) AND MONTH(h.ngayTao) = MONTH(:now) AND YEAR(h.ngayTao) = YEAR(:now) AND h.trangThai = :trangThai")
    Float getDoanhSoNgayNay(@Param("now") LocalDateTime now, @Param("trangThai") String trangThai);


    @Query("SELECT COALESCE(SUM(l.soTienThanhToan), 0) " +
            "FROM LichSuThanhToan l " +
            "WHERE YEAR(l.thoiGianThanhToan) = YEAR(:now)")
    Float getDoanhThuNamNay(@Param("now") LocalDateTime now);

    @Query("SELECT COALESCE(SUM(l.soTienThanhToan), 0) " +
            "FROM LichSuThanhToan l " +
            "WHERE MONTH(l.thoiGianThanhToan) = MONTH(:now) AND YEAR(l.thoiGianThanhToan) = YEAR(:now)")
    Float getDoanhThuThangNay(@Param("now") LocalDateTime now);

    @Query("SELECT COALESCE(SUM(l.soTienThanhToan), 0) " +
            "FROM LichSuThanhToan l " +
            "WHERE DAY(l.thoiGianThanhToan) = DAY(:now) " +
            "AND MONTH(l.thoiGianThanhToan) = MONTH(:now) " +
            "AND YEAR(l.thoiGianThanhToan) = YEAR(:now)")
    Float getDoanhThuNgayNay(@Param("now") LocalDateTime now);


    @Query("SELECT COUNT(h) FROM HoaDon h WHERE MONTH(h.ngayTao) = MONTH(:now) AND YEAR(h.ngayTao) = YEAR(:now) AND h.trangThai = :trangThai")
    int countHoaDonThangNay(@Param("now") LocalDateTime now, @Param("trangThai") String trangThai);


    @Query("SELECT COUNT(h) FROM HoaDon h WHERE DAY(h.ngayTao) = DAY(:now) AND MONTH(h.ngayTao) = MONTH(:now) AND YEAR(h.ngayTao) = YEAR(:now) AND h.trangThai = :trangThai")
    int countHoaDonNgayNay(@Param("now") LocalDateTime now, @Param("trangThai") String trangThai);

    // Tính tổng tiền trong một khoảng thời gian
    @Query("SELECT SUM(h.tongTien) FROM HoaDon h WHERE h.ngayTao BETWEEN :startDate AND :endDate")
    Float sumTongTienByNgayTaoBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT YEAR(hd.ngayTao), MONTH(hd.ngayTao), DAY(hd.ngayTao), COUNT(hd) " +
            "FROM HoaDon hd " +
            "WHERE hd.trangThai = :trangThai " +
            "GROUP BY YEAR(hd.ngayTao), MONTH(hd.ngayTao), DAY(hd.ngayTao) " +
            "ORDER BY YEAR(hd.ngayTao), MONTH(hd.ngayTao), DAY(hd.ngayTao)")
    List<Object[]> getInvoiceCountByDateMonthYear(@Param("trangThai") String trangThai);


    @Query("SELECT COUNT(hd) FROM HoaDon hd WHERE FUNCTION('MONTH', hd.ngayTao) = :month AND FUNCTION('YEAR', hd.ngayTao) = :year")
    Long getSoLuongHoaDonThangNay(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.ngayTao BETWEEN :startDate AND :endDate")
    Integer countHoaDonByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.ngayTao BETWEEN :startDate AND :endDate")
    public Integer countHoaDonByNgayTao(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Integer countHoaDonByNgayTaoBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<HoaDon> findByTrangThai(String trangThai, Sort sort);

    Integer countByTrangThai(String trangThai);

}
