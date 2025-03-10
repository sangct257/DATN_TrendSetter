package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.SanPham;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham,Integer> {

    Optional<SanPham> findByTenSanPham(String tenSanPham);


    List<SanPham> findByTrangThai(String trangThai, Sort sort);

    Integer countByTrangThai(String trangThai);

}
