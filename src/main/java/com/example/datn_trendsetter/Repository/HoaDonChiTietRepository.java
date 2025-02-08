package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.HoaDonChiTiet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet,Integer> {
    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);

    Page<HoaDonChiTiet> findByHoaDon_Id(Integer hoaDonId, Pageable pageable);

    Optional<HoaDonChiTiet> findByHoaDonIdAndSanPhamChiTietId(Integer hoaDonId, Integer sanPhamChiTietId);
}
