package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.DTO.SanPhamChiTietDTO;
import com.example.datn_trendsetter.Entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {
    List<SanPhamChiTiet> findBySanPham(SanPham sanPham);

    // Phương thức tìm các sản phẩm có trạng thái "Còn Hàng"
    List<SanPhamChiTiet> findByTrangThai(String trangThai);

    @Query("SELECT spct FROM SanPhamChiTiet spct " +
            "JOIN FETCH spct.sanPham sp " +
            "JOIN FETCH sp.thuongHieu th " +
            "JOIN FETCH sp.xuatXu xx " +
            "WHERE spct.trangThai = :trangThai")
    Page<SanPhamChiTiet> findByTrangThai(@Param("trangThai") String trangThai, Pageable pageable);


    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.soLuong <= 10 AND spct.deleted = false ORDER BY spct.soLuong ASC")
    Page<SanPhamChiTiet> findLowStockProducts(Pageable pageable);

    @Query("SELECT s FROM SanPhamChiTiet s " +
            "JOIN s.sanPham sp " +
            "JOIN s.mauSac ms " +
            "JOIN s.kichThuoc kt " +
            "WHERE LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(ms.tenMauSac) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(kt.tenKichThuoc) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "AND s.deleted = false")
    Page<SanPhamChiTiet> searchSanPhamChiTiet(@Param("search") String search, Pageable pageable);

    @Query("SELECT new com.example.datn_trendsetter.DTO.SanPhamChiTietDTO(" +
            "sp.tenSanPham, ms.tenMauSac, kt.tenKichThuoc) " +
            "FROM SanPhamChiTiet s " +
            "JOIN s.sanPham sp " +
            "JOIN s.mauSac ms " +
            "JOIN s.kichThuoc kt " +
            "WHERE (LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(ms.tenMauSac) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(kt.tenKichThuoc) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND s.deleted = false")
    List<SanPhamChiTietDTO> suggestSanPhamAndMauSacAndKichThuoc(@Param("search") String search);




}
