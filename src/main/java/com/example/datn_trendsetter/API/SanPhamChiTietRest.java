package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.SanPhamChiTietViewDTO;
import com.example.datn_trendsetter.DTO.StockUpdateRequest;
import com.example.datn_trendsetter.Service.SanPhamChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/san-pham-chi-tiet")
@CrossOrigin("*")
public class SanPhamChiTietRest {
    @Autowired
    private SanPhamChiTietService sanPhamChiTietService;

    @GetMapping("/{id}")
    public ResponseEntity<List<SanPhamChiTietViewDTO>> getChiTietSanPham(@PathVariable Integer id) {
        List<SanPhamChiTietViewDTO> chiTietSanPham = sanPhamChiTietService.getChiTietSanPhamById(id);
        if (chiTietSanPham.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chiTietSanPham);
    }

    @PostMapping("/reduce-stock")
    public ResponseEntity<Map<String, String>> reduceStock(@RequestBody List<StockUpdateRequest> stockUpdates) {
        Map<String, String> results = sanPhamChiTietService.reduceStock(stockUpdates);
        return ResponseEntity.ok(results);
    }

}
