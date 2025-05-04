package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.NhanVien;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    Optional<NhanVien> findByEmail(String email);
    List<NhanVien> findByDeleted(Boolean deleted, Sort sort);

    Optional<NhanVien> findFirstByUsername(String username);

    boolean existsByEmail(String email);
}
