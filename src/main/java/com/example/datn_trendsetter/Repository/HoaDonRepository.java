package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon,Integer> {
    // Đếm số lượng hóa đơn có trạng thái cụ thể
    long countByTrangThai(String trangThai);

    // Lấy danh sách hóa đơn theo trạng thái
    List<HoaDon> findByTrangThai(String trangThai);

    // Truy vấn đếm số hóa đơn theo trạng thái
    @Query("SELECT h.trangThai, COUNT(h) FROM HoaDon h GROUP BY h.trangThai")
    List<Object[]> countByTrangThai();

    // Tìm hóa đơn theo trạng thái và phân trang
    Page<HoaDon> findByTrangThai(String trangThai, Pageable pageable);

}
