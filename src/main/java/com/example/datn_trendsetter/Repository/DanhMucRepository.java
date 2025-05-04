package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.DanhMuc;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc,Integer> {
    boolean existsByTenDanhMuc(String tenDanhMuc);

    List<DanhMuc> findByTrangThai(String trangThai, Sort sort);

    List<DanhMuc> findByDeleted(Boolean deleted, Sort sort);

    @Query("SELECT d FROM DanhMuc d WHERE NOT EXISTS (SELECT 1 FROM SanPham s WHERE s.danhMuc = d AND s.trangThai = 'Đang Hoạt Động')")
    List<DanhMuc> findDanhMucWithoutActiveProducts();

}
