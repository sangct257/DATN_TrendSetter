package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang,Integer> {
    Page<KhachHang> findAllByTrangThai(String trangThai,Pageable pageable);
    Optional<KhachHang> findByEmail(String email);
    Optional<KhachHang> findByUsername(String username);

}
