package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia,Integer> {
    Optional<PhieuGiamGia> findByTenChuongTrinh(String tenChuongTrinh);
}
