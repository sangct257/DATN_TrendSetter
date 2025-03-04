package com.example.datn_trendsetter.Repository;


import com.example.datn_trendsetter.Entity.ThuongHieu;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu,Integer> {
    boolean existsByTenThuongHieu(String tenThuongHieu);

    List<ThuongHieu> findByTrangThai(String trangThai, Sort sort);
}
