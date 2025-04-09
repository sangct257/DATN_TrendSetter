package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.SanPham;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham,Integer> {

    Optional<SanPham> findByTenSanPham(String tenSanPham);


    List<SanPham> findByTrangThai(String trangThai, Sort sort);

    List<SanPham> findByDeleted(Boolean deleted,Sort sort);
    Integer countByTrangThai(String trangThai);

    @Modifying
    @Query("UPDATE SanPham sp SET sp.soLuong = :totalStock WHERE sp.id = :idSanPham")
    void updateStock(@Param("idSanPham") Integer idSanPham, @Param("totalStock") Integer totalStock);


}
