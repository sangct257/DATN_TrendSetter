package com.example.datn_trendsetter.Component;
import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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

        for (PhieuGiamGia pgg : phieuGiamGiaList) {
            if (pgg == null) continue;

            boolean isEditedByAdmin = editedByAdmin.getOrDefault(pgg.getId(), false);

            if (isEditedByAdmin) {
                continue; // Bỏ qua cập nhật nếu ADMIN đã thay đổi
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

                // Nếu phiếu đã hết hạn, khóa luôn trạng thái (không cho nhân viên chỉnh sửa nữa)
                if (newStatus.equals("Ngừng Hoạt Động") && today.isAfter(pgg.getNgayKetThuc())) {
                    editedByAdmin.put(pgg.getId(), true); // Khóa trạng thái
                }

                // Scheduler chỉ cập nhật nếu ADMIN chưa thay đổi
                if (!isEditedByAdmin) {
                    if (!pgg.getTrangThai().equals(newStatus)) {
                        pgg.setTrangThai(newStatus);
                        phieuGiamGiaRepository.save(pgg);
                    }
                }
            }
        }
    }

    // Cập nhật cờ trong bộ nhớ tạm thời khi ADMIN thay đổi trạng thái
    public static void markAsEditedByAdmin(Integer id) {
        editedByAdmin.put(id, true);
    }

    // Xóa cờ trong bộ nhớ tạm thời khi ADMIN cho phép thay đổi lại
    public static void removeEditedFlag(Integer id) {
        editedByAdmin.remove(id);
    }
}
