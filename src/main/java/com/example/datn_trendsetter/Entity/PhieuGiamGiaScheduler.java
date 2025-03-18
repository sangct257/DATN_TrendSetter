package com.example.datn_trendsetter.Entity;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PhieuGiamGiaScheduler {

    private final PhieuGiamGiaRepository phieuGiamGiaRepository;

    public PhieuGiamGiaScheduler(PhieuGiamGiaRepository phieuGiamGiaRepository) {
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
    }
    @Scheduled(fixedRate = 1) // Chạy mỗi phút (60 giây)
    public void updatePhieuGiamGiaStatus() {
        List<PhieuGiamGia> phieuGiamGiaList = phieuGiamGiaRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        LocalDate today = LocalDate.now();

        // Kiểm tra nếu danh sách là null hoặc rỗng
        if (phieuGiamGiaList == null || phieuGiamGiaList.isEmpty()) {
            return; // Nếu không có phiếu giảm giá nào, thoát khỏi phương thức
        }

        for (PhieuGiamGia pgg : phieuGiamGiaList) {
            // Kiểm tra nếu pgg là null
            if (pgg == null) {
                continue; // Nếu phiếu giảm giá là null, bỏ qua và chuyển sang phần tử tiếp theo
            }

            if (pgg.getNgayBatDau() != null && pgg.getNgayKetThuc() != null) {
                String newStatus;
                if (today.isBefore(pgg.getNgayBatDau())) {
                    newStatus = "Sắp Diễn Ra";
                } else if (!today.isBefore(pgg.getNgayBatDau()) && !today.isAfter(pgg.getNgayKetThuc())) {
                    newStatus = "Đang Hoạt Động";
                } else {
                    newStatus = "Ngừng Hoạt Động";
                }

                if (!pgg.getTrangThai().equals(newStatus)) {
                    pgg.setTrangThai(newStatus);
                    phieuGiamGiaRepository.save(pgg); // Lưu nếu trạng thái thay đổi
                }
            }
        }
    }

}

