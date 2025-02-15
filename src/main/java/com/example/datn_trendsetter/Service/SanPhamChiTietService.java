package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.SanPhamChiTiet;
import com.example.datn_trendsetter.Repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SanPhamChiTietService {
    @Autowired
    SanPhamChiTietRepository sanPhamChiTietRepository;

    public Page<SanPhamChiTiet> findLowStockProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size); // page bắt đầu từ 0
        return sanPhamChiTietRepository.findLowStockProducts(pageable); // soLuong < 10 và deleted = false
    }

}
