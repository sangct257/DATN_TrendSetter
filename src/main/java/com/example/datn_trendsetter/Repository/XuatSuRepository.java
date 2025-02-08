package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.XuatXu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XuatSuRepository extends JpaRepository<XuatXu,Integer> {
    boolean existsByQuocGia(String quocGia);
}
