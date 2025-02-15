package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.SanPhamChiTietDTO;
import com.example.datn_trendsetter.Entity.SanPhamChiTiet;
import com.example.datn_trendsetter.Repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ShopApiController {
    @Autowired
    SanPhamChiTietRepository sanPhamChiTietRepository;

    @GetMapping("/suggest-products")
    @ResponseBody
    public ResponseEntity<List<SanPhamChiTietDTO>> suggestProducts(@RequestParam("search") String search) {
        List<SanPhamChiTietDTO> suggestions = sanPhamChiTietRepository.suggestSanPhamAndMauSacAndKichThuoc(search);
        return ResponseEntity.ok(suggestions);
    }





}
