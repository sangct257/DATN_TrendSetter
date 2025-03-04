package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon,Integer> {
    List<LichSuHoaDon> findByHoaDon_Id(Integer hoaDonId);

    List<LichSuHoaDon> findByHoaDonId(Integer id);
}
