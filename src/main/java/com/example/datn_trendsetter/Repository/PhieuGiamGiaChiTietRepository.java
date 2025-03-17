package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.PhieuGiamGiaChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhieuGiamGiaChiTietRepository extends JpaRepository<PhieuGiamGiaChiTiet, Integer> {
    List<PhieuGiamGiaChiTiet> findByPhieuGiamGiaId(Integer phieuGiamGiaId);

    // Xóa tất cả chi tiết của một phiếu giảm giá (cần khi update)
    void deleteByPhieuGiamGia_Id(Integer phieuGiamGiaId);
}
