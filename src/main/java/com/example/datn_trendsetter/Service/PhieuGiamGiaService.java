package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PhieuGiamGiaService {
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;

    @PostConstruct
    public void initPhieuGiamGia() {
        if (phieuGiamGiaRepository.count() == 0) {
            PhieuGiamGia pgg1 = new PhieuGiamGia();
            pgg1.setTenChuongTrinh("GIAM50K");
            pgg1.setGiaTri(50000F);
            pgg1.setDieuKien(300000F);
            pgg1.setNgayBatDau(LocalDateTime.now().minusDays(1));
            pgg1.setNgayKetThuc(LocalDateTime.now().plusDays(30));
            pgg1.setMoTa("Giảm 50.000đ cho đơn hàng từ 300.000đ");
            pgg1.setTrangThai("Hoạt động");
            pgg1.setNgayTao(LocalDate.now());
            pgg1.setSoLuotSuDung(100);
            pgg1.setSoLuotDaDung(0);
            pgg1.setLoaiApDung("PERCENTAGE");
            pgg1.setIsActive(true);
            pgg1.setDeleted(false);

            PhieuGiamGia pgg2 = new PhieuGiamGia();
            pgg2.setTenChuongTrinh("GIAM10%");
            pgg2.setGiaTri(10F);
            pgg2.setDieuKien(500000F);
            pgg2.setNgayBatDau(LocalDateTime.now().minusDays(1));
            pgg2.setNgayKetThuc(LocalDateTime.now().plusDays(30));
            pgg2.setMoTa("Giảm 10% cho đơn hàng từ 500.000đ");
            pgg2.setTrangThai("Hoạt động");
            pgg2.setNgayTao(LocalDate.now());
            pgg2.setSoLuotSuDung(200);
            pgg2.setSoLuotDaDung(0);
            pgg2.setLoaiApDung("FIXED_AMOUNT");
            pgg2.setIsActive(true);
            pgg2.setDeleted(false);

            phieuGiamGiaRepository.save(pgg1);
            phieuGiamGiaRepository.save(pgg2);
        }
    }
}
