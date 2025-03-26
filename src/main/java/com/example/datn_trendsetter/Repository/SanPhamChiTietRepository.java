package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.DTO.SanPhamChiTietDTO;
import com.example.datn_trendsetter.DTO.SanPhamViewDTO;
import com.example.datn_trendsetter.Entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {
    List<SanPhamChiTiet> findBySanPham(SanPham sanPham);
    List<SanPhamChiTiet> findAllByIdIn(List<Integer> ids);

    // Phương thức tìm các sản phẩm có trạng thái "Còn Hàng"
    List<SanPhamChiTiet> findByTrangThai(String trangThai);

    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.soLuong <= 10")
    List<SanPhamChiTiet> findLowStockProducts();


    List<SanPhamChiTiet> findBySanPhamId(Integer sanPhamId);

    boolean existsBySanPhamAndMauSacIdAndKichThuocId(SanPham sanPham, Integer mauSacId, Integer kichThuocId);

    Integer Id(Integer id);

    List<SanPhamChiTiet> findByTrangThai(String trangThai, Sort sort);

    @Query("SELECT new com.example.datn_trendsetter.DTO.SanPhamViewDTO( " +
            "sp.id, sp.tenSanPham, MIN(spct.gia), MIN(ha.urlHinhAnh), sp.trangThai) " +
            "FROM SanPhamChiTiet spct " +
            "JOIN spct.sanPham sp " +
            "LEFT JOIN spct.hinhAnh ha " +
            "WHERE sp.deleted = false " +
            "GROUP BY sp.id, sp.tenSanPham, sp.trangThai")
    Page<SanPhamViewDTO> findSanPhamChiTiet(Pageable pageable);

    @Query("SELECT spct.id, sp.tenSanPham, spct.gia, sp.moTa, kt.tenKichThuoc, ms.tenMauSac, ha.urlHinhAnh, spct.soLuong,spct.trangThai " +
            "FROM SanPhamChiTiet spct " +
            "JOIN spct.sanPham sp " +
            "JOIN spct.kichThuoc kt " +
            "JOIN spct.mauSac ms " +
            "LEFT JOIN HinhAnh ha ON ha.sanPhamChiTiet.id = spct.id " +
            "WHERE sp.id = :idSanPham AND sp.deleted = false")
    List<Object[]> findSanPhamChiTietWithImages(@Param("idSanPham") Integer idSanPham);

    @Modifying
    @Query("UPDATE SanPhamChiTiet spct SET spct.soLuong = spct.soLuong - :soLuong WHERE spct.id = :idSanPhamChiTiet AND spct.soLuong >= :soLuong")
    int reduceStock(@Param("idSanPhamChiTiet") Integer idSanPhamChiTiet, @Param("soLuong") Integer soLuong);

    @Query("SELECT spct.sanPham.id FROM SanPhamChiTiet spct WHERE spct.id = :idSanPhamChiTiet")
    Integer findSanPhamIdByChiTietId(@Param("idSanPhamChiTiet") Integer idSanPhamChiTiet);

    @Query("SELECT COALESCE(SUM(spct.soLuong), 0) FROM SanPhamChiTiet spct WHERE spct.sanPham.id = :idSanPham")
    Integer getTotalStock(@Param("idSanPham") Integer idSanPham);

    @Query("SELECT SUM(s.soLuong) FROM SanPhamChiTiet s WHERE s.sanPham.id = :sanPhamId")
    Integer tinhTongSoLuongTheoSanPham(@Param("sanPhamId") Integer sanPhamId);

}
