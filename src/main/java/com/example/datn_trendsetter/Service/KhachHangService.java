package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.KhachHangDTO;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KhachHangService {
    @Autowired
    private KhachHangRepository khachHangRepository;

    public List<KhachHangDTO> getAllKhachHang() {
        List<KhachHang> khachHangs = khachHangRepository.findAll();
        return khachHangs.stream().map(kh ->
                new KhachHangDTO(kh.getId(), kh.getHoTen(), kh.getEmail(), kh.getSoDienThoai(), kh.getGioiTinh())
        ).collect(Collectors.toList());
    }
    public List<KhachHang> searchKhachHang(String keyword) {
        return khachHangRepository.searchKhachHang(keyword);
    }
}
