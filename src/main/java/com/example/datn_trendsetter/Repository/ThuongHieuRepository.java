package com.example.datn_trendsetter.Repository;


import com.example.datn_trendsetter.Entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu,Integer> {
    boolean existsByTenThuongHieu(String tenThuongHieu);
}
