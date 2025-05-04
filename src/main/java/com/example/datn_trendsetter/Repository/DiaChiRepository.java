package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.DiaChi;
import com.example.datn_trendsetter.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaChiRepository extends JpaRepository<DiaChi,Integer> {
    DiaChi findByKhachHangAndTrangThai(KhachHang khachHang, String trangThai);

    Optional<DiaChi> findByKhachHangIdAndTrangThai(Integer idKhachHang, String trangThai);

    List<DiaChi> findByKhachHang_Id(Integer khachHangId);

    List<DiaChi> findByKhachHangId(Integer khachHangId);

    @Modifying
    @Query("UPDATE DiaChi d SET d.trangThai = :trangThai WHERE d.khachHang.id = :khachHangId")
    void updateAllToKhongMacDinh(@Param("khachHangId") Integer khachHangId,
                                 @Param("trangThai") String trangThai);



    boolean existsByKhachHangAndTrangThai(KhachHang khachHang, String trangThai);

    Integer countByKhachHangId(Integer idKhachHang);

    DiaChi findByIdAndKhachHangId(Integer id, Integer id1);
}
