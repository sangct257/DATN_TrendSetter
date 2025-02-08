package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.KichThuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KichThuocRepository extends JpaRepository<KichThuoc,Integer> {
    boolean existsByTenKichThuoc(String tenKichThuoc);
}
