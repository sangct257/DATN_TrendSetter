package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.SanPham;
import com.example.datn_trendsetter.Repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
public class SanPhamAPIController {
    @Autowired
    private SanPhamRepository sanPhamRepository;


    @PostMapping("/add")
    public ResponseEntity<?> themSanPham(@RequestBody SanPham sanPham) {
        if (sanPham.getTenSanPham() == null || sanPham.getTenSanPham().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên sản phẩm không được để trống!");
        }

        sanPham.setNgayTao(LocalDate.now());
        sanPham.setTrangThai("Hoạt động");
        sanPham.setDeleted(false);

        SanPham savedSanPham = sanPhamRepository.save(sanPham);
        return ResponseEntity.ok(savedSanPham);
    }


}
