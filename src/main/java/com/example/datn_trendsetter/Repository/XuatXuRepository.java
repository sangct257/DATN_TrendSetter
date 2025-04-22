package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.XuatXu;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface XuatXuRepository extends JpaRepository<XuatXu,Integer> {
    boolean existsByQuocGia(String quocGia);

    List<XuatXu> findByTrangThai(String trangThai, Sort sort);

    List<XuatXu> findByDeleted(Boolean deleted, Sort sort);

}
