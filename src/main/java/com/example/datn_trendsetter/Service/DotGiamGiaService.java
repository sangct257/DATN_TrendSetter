package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.DotGiamGia;
import com.example.datn_trendsetter.Entity.SanPham;
import com.example.datn_trendsetter.Repository.DotGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DotGiamGiaService {
    @Autowired
    private DotGiamGiaRepository dotGiamGiaRepository;

    public boolean toggleDotGiamGiaStatus(Integer id) {
        Optional<DotGiamGia> optionalDotGiamGia = dotGiamGiaRepository.findById(id);
        if (optionalDotGiamGia.isPresent()) {
            DotGiamGia dotGiamGia = optionalDotGiamGia.get();

            // Thay đổi trạng thái và đánh dấu xóa mềm
            if ("Đang Hoạt Động".equals(dotGiamGia.getTrangThai())) {
                dotGiamGia.setTrangThai("Ngừng Hoạt Động");
                dotGiamGia.setDeleted(true);
            } else {
                dotGiamGia.setTrangThai("Đang Hoạt Động");
                dotGiamGia.setDeleted(false);
            }

            dotGiamGiaRepository.save(dotGiamGia);
            return true;
        }
        return false;
    }

}
