package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.SanPhamChiTiet;
import com.example.datn_trendsetter.Repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SanPhamChiTietService {
    @Autowired
    SanPhamChiTietRepository sanPhamChiTietRepository;

    public List<SanPhamChiTiet> findLowStockProducts() {
        return sanPhamChiTietRepository.findLowStockProducts(); // soLuong < 10 và deleted = false
    }

}
