package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.DTO.HoaDonResponseDto;
import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Entity.PhieuGiamGia;
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
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    // Lấy danh sách hóa đơn theo trạng thái
    List<HoaDon> findByTrangThai(String trangThai);

    @Query("select hd from HoaDon hd order by hd.id desc")
    List<HoaDon> getAllHoaDon();

    List<HoaDon> findByTrangThaiNot(String trangThai,Sort sort);

    HoaDon findByMaHoaDon(String maHoaDon);

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


    @Query("SELECT COALESCE(SUM(l.tongTien), 0) " +
            "FROM HoaDon l " +
            "WHERE YEAR(l.ngayTao) = YEAR(:now) AND l.trangThai = :trangThai")
    Float getDoanhThuNamNay(@Param("now") LocalDateTime now,String trangThai);

    @Query("SELECT COALESCE(SUM(l.tongTien), 0) " +
            "FROM HoaDon l " +
            "WHERE MONTH(l.ngayTao) = MONTH(:now) AND YEAR(l.ngayTao) = YEAR(:now) AND l.trangThai = :trangThai")
    Float getDoanhThuThangNay(@Param("now") LocalDateTime now,String trangThai);

    @Query("SELECT COALESCE(SUM(l.tongTien), 0) " +
            "FROM HoaDon l " +
            "WHERE DAY(l.ngayTao) = DAY(:now) " +
            "AND MONTH(l.ngayTao) = MONTH(:now) " +
            "AND YEAR(l.ngayTao) = YEAR(:now) AND l.trangThai = :trangThai")
    Float getDoanhThuNgayNay(@Param("now") LocalDateTime now,String trangThai);


    @Query("SELECT COUNT(h) FROM HoaDon h WHERE MONTH(h.ngayTao) = MONTH(:now) AND YEAR(h.ngayTao) = YEAR(:now) AND h.trangThai = :trangThai")
    int countHoaDonThangNay(@Param("now") LocalDateTime now, @Param("trangThai") String trangThai);


    @Query("SELECT COUNT(h) FROM HoaDon h WHERE DAY(h.ngayTao) = DAY(:now) AND MONTH(h.ngayTao) = MONTH(:now) AND YEAR(h.ngayTao) = YEAR(:now) AND h.trangThai = :trangThai")
    int countHoaDonNgayNay(@Param("now") LocalDateTime now, @Param("trangThai") String trangThai);

    // Tính tổng tiền trong một khoảng thời gian
    @Query("SELECT SUM(h.tongTien) FROM HoaDon h WHERE h.ngayTao BETWEEN :startDate AND :endDate")
    Float sumTongTienByNgayTaoBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT YEAR(hd.ngayTao), MONTH(hd.ngayTao), DAY(hd.ngayTao), COUNT(DISTINCT hd.id) " +
            "FROM HoaDon hd " +
            "WHERE hd.trangThai IN :trangThaiHoaDon " +
            "GROUP BY YEAR(hd.ngayTao), MONTH(hd.ngayTao), DAY(hd.ngayTao) " +
            "ORDER BY YEAR(hd.ngayTao), MONTH(hd.ngayTao), DAY(hd.ngayTao)")
    List<Object[]> getInvoiceCountByDateMonthYear(
            @Param("trangThaiHoaDon") List<String> trangThaiHoaDon);

    @Query("SELECT YEAR(hd.ngayTao), MONTH(hd.ngayTao), DAY(hd.ngayTao), SUM(hd.tongTien) " +
            "FROM HoaDon hd " +
            "WHERE hd.trangThai IN :trangThaiHoaDon " +
            "GROUP BY YEAR(hd.ngayTao), MONTH(hd.ngayTao), DAY(hd.ngayTao) " +
            "ORDER BY YEAR(hd.ngayTao), MONTH(hd.ngayTao), DAY(hd.ngayTao)")
    List<Object[]> getTotalRevenueByDateMonthYear(
            @Param("trangThaiHoaDon") List<String> trangThaiHoaDon
    );

    @Query("SELECT h FROM HoaDon h WHERE h.trangThai NOT IN :trangThaiList")
    List<HoaDon> findByTrangThaiNotInCustom(@Param("trangThaiList") List<String> trangThaiList, Sort sort);



    @Query("SELECT COUNT(hd) FROM HoaDon hd WHERE FUNCTION('MONTH', hd.ngayTao) = :month AND FUNCTION('YEAR', hd.ngayTao) = :year")
    Long getSoLuongHoaDonThangNay(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.ngayTao BETWEEN :startDate AND :endDate")
    Integer countHoaDonByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.ngayTao BETWEEN :startDate AND :endDate")
    public Integer countHoaDonByNgayTao(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Integer countHoaDonByNgayTaoBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<HoaDon> findByTrangThai(String trangThai, Sort sort);

    Integer countByTrangThai(String trangThai);

    @Query("SELECT new com.example.datn_trendsetter.DTO.HoaDonResponseDto(h.id, h.maHoaDon, " +
            "h.nguoiNhan, h.nguoiTao,h.loaiHoaDon," +
            "h.ngayTao, h.phieuGiamGia.giaTriGiam, h.tongTien) " +
            "FROM HoaDon h WHERE h.khachHang.id = :khachHangId")
    List<HoaDonResponseDto> findHoaDonByKhachHangId(@Param("khachHangId") Integer khachHangId);

    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.id = :khachHangId")
    List<HoaDon> findByKhachHangId(@Param("khachHangId") Integer khachHangId);

    @Query("SELECT h FROM HoaDon h JOIN FETCH h.khachHang WHERE h.khachHang.id = :khachHangId")
    List<HoaDon> findByKhachHangIdWithKhachHang(@Param("khachHangId") Integer khachHangId);

    List<HoaDon> findByKhachHang_Id(Integer khachHangId);

    Integer countByTrangThaiNot(String trangThai);

    boolean existsByPhieuGiamGia(PhieuGiamGia phieuGiamGia);
}
