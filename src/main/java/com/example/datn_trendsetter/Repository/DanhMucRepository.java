package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc,Integer> {
    boolean existsByTenDanhMuc(String tenDanhMuc);
}
