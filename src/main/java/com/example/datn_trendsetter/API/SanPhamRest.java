package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.SanPhamViewDTO;
import com.example.datn_trendsetter.Service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sanpham")

public class SanPhamRest {
    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping
    public ResponseEntity<Page<SanPhamViewDTO>> getSanPhams(@RequestParam(defaultValue = "0") int page) {
        Page<SanPhamViewDTO> sanPhams = sanPhamService.getSanPhams(page, 6);
        return ResponseEntity.ok(sanPhams);
    }
}
