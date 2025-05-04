package com.example.datn_trendsetter.Component;

import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class PhieuGiamGiaScheduler {

    private static final ConcurrentHashMap<Integer, Boolean> editedByAdmin = new ConcurrentHashMap<>();

    private final PhieuGiamGiaRepository phieuGiamGiaRepository;

    public PhieuGiamGiaScheduler(PhieuGiamGiaRepository phieuGiamGiaRepository) {
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
    }

    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    public void updatePhieuGiamGiaStatus() {
        List<PhieuGiamGia> phieuGiamGiaList = phieuGiamGiaRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        LocalDate today = LocalDate.now();

        if (phieuGiamGiaList.isEmpty()) {
            return;
        }

        ReentrantLock lock = new ReentrantLock();

        for (PhieuGiamGia pgg : phieuGiamGiaList) {
            if (pgg == null) continue;

            lock.lock();
            try {
                boolean isEditedByAdmin = editedByAdmin.getOrDefault(pgg.getId(), false);
                if (isEditedByAdmin) {
                    continue; // Bỏ qua nếu ADMIN đã can thiệp
                }

                String newStatus = pgg.getTrangThai(); // Mặc định giữ nguyên
                LocalDate startDate = pgg.getNgayBatDau();
                LocalDate endDate = pgg.getNgayKetThuc();

                if (startDate != null && endDate != null) {
                    if (pgg.getSoLuotSuDung() <= 0 &&
                            !today.isBefore(startDate) &&
                            !today.isAfter(endDate)) {
                        newStatus = "Ngừng Hoạt Động";
                    } else {
                        if (today.isBefore(startDate)) {
                            newStatus = "Sắp Diễn Ra";
                        } else if (!today.isAfter(endDate)) {
                            newStatus = "Đang Hoạt Động";
                        } else {
                            newStatus = "Ngừng Hoạt Động";
                        }
                    }
                }

                if (!pgg.getTrangThai().equals(newStatus)) {
                    pgg.setTrangThai(newStatus);
                    phieuGiamGiaRepository.save(pgg);
                }

                // Nếu đã hết hạn, có thể tự động đánh dấu không cho chỉnh nữa
                if ("Ngừng Hoạt Động".equals(newStatus) && today.isAfter(endDate)) {
                    editedByAdmin.put(pgg.getId(), true);
                }

            } finally {
                lock.unlock();
            }
        }
    }

    // Được gọi khi ADMIN thay đổi trạng thái
    public static void markAsEditedByAdmin(Integer id) {
        editedByAdmin.put(id, true);
    }

    // ADMIN có thể mở lại để Scheduler được phép can thiệp tiếp
    public static void removeEditedFlag(Integer id) {
        editedByAdmin.remove(id);
    }
}
