package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang,Integer> {
    Page<KhachHang> findAllByTrangThai(String trangThai,Pageable pageable);
    Optional<KhachHang> findByEmail(String email);
    Optional<KhachHang> findByUsername(String username);

    @Query("SELECT k FROM KhachHang k WHERE " +
            "LOWER(k.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(k.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "k.soDienThoai LIKE CONCAT('%', :keyword, '%')")
    List<KhachHang> searchKhachHang(@Param("keyword") String keyword);
}
