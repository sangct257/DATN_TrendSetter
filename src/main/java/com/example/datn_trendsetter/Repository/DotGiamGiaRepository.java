package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.DotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia,Integer> {
}
