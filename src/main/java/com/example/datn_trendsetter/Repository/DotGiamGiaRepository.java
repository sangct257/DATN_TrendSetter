package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.DotGiamGia;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia,Integer> {

    List<DotGiamGia> findByTrangThai(String trangThai, Sort sort);

    Integer countByTrangThai(String trangThai);

}
