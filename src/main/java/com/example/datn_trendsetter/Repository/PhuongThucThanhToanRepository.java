package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.PhuongThucThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhuongThucThanhToanRepository extends JpaRepository<PhuongThucThanhToan,Integer> {

    // Lấy phương thức thanh toán đầu tiên theo ID tăng dần
    PhuongThucThanhToan findFirstByOrderByIdAsc();

    PhuongThucThanhToan findByTenPhuongThuc(String tenPhuongThuc);

}
