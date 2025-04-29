package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    Optional<NhanVien> findByEmail(String email); // Đảm bảo trả về Optional

    Optional<NhanVien> findFirstByUsername(String username);

    boolean existsByEmail(String email);
}
