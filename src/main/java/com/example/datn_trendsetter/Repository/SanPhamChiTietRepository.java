package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet,Integer>{

}
