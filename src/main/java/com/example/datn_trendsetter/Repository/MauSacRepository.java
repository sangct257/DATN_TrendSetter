package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac,Integer> {
    boolean existsByTenMauSac(String tenMauSac);
}
