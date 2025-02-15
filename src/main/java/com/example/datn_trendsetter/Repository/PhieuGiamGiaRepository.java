package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia,Integer> {
    Optional<PhieuGiamGia> findByTenChuongTrinh(String tenChuongTrinh);

    List<PhieuGiamGia> findAllByTrangThai(@Param("trangThai") String trangThai);
}
