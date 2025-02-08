package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet,Integer>{
    List<SanPhamChiTiet> findBySanPham(SanPham sanPham);

    // Phương thức tìm các sản phẩm có trạng thái "Còn Hàng"
    List<SanPhamChiTiet> findByTrangThai(String trangThai);

    Optional<SanPhamChiTiet> findBySanPhamAndMauSacAndKichThuocAndChatLieu(
            SanPham sanPham, MauSac mauSac, KichThuoc kichThuoc, ChatLieu chatLieu);

    Page<SanPhamChiTiet> findBySanPham_Id(Integer sanPhamId, Pageable pageable);
}
