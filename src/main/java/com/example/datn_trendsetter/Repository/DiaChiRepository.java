package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.DiaChi;
import com.example.datn_trendsetter.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaChiRepository extends JpaRepository<DiaChi,Integer> {
    DiaChi findByKhachHangAndTrangThai(KhachHang khachHang, String trangThai);

}
