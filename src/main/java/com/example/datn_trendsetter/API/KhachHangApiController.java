package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.KhachHangDTO;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/khach-hang")
public class KhachHangApiController {
    @Autowired
    private KhachHangService khachHangService;

    @GetMapping
    public ResponseEntity<List<KhachHangDTO>> getAllKhachHang() {
        return ResponseEntity.ok(khachHangService.getAllKhachHang());
    }
    @GetMapping("/search")
    public ResponseEntity<List<KhachHang>> searchKhachHang(@RequestParam String keyword) {
        List<KhachHang> result = khachHangService.searchKhachHang(keyword);
        return ResponseEntity.ok(result);
    }
}
