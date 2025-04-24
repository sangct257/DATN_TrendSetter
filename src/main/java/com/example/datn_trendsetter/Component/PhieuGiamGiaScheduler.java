package com.example.datn_trendsetter.Component;

import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class PhieuGiamGiaScheduler {
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;

    public PhieuGiamGiaScheduler(PhieuGiamGiaRepository phieuGiamGiaRepository) {
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
    }

    @Scheduled(fixedRate = 1) // Chạy mỗi phút
    public void updatePhieuGiamGiaStatus() {
        List<PhieuGiamGia> phieuGiamGiaList = phieuGiamGiaRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        LocalDate today = LocalDate.now();

        if (phieuGiamGiaList.isEmpty()) {
            return;
        }

        ReentrantLock lock = new ReentrantLock();  // Khóa để đảm bảo chỉ một luồng truy cập tại một thời điểm

        for (PhieuGiamGia pgg : phieuGiamGiaList) {
            if (pgg == null) continue;

            lock.lock();
            try {
                String newStatus = pgg.getTrangThai(); // Mặc định giữ nguyên

                LocalDate startDate = pgg.getNgayBatDau();
                LocalDate endDate = pgg.getNgayKetThuc();

                if (startDate != null && endDate != null) {
                    // Nếu đã hết lượt sử dụng và đang trong thời gian hoạt động
                    if (pgg.getSoLuotSuDung() <= 0 &&
                            !today.isBefore(startDate) &&
                            !today.isAfter(endDate)) {
                        newStatus = "Ngừng Hoạt Động";
                    } else {
                        // Xử lý trạng thái theo ngày
                        if (today.isBefore(startDate)) {
                            newStatus = "Sắp Diễn Ra";
                        } else if (!today.isAfter(endDate)) {
                            newStatus = "Đang Hoạt Động";
                        } else {
                            newStatus = "Ngừng Hoạt Động";
                        }
                    }
                }

                // Chỉ cập nhật nếu trạng thái thay đổi
                if (!pgg.getTrangThai().equals(newStatus)) {
                    pgg.setTrangThai(newStatus);
                    phieuGiamGiaRepository.save(pgg);
                }

            } finally {
                lock.unlock();
            }
        }
    }
}
