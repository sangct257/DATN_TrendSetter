package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SanPhamRepository extends JpaRepository<SanPham,Integer> {

}
