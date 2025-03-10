package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.PhieuGiamGiaDTO;
import com.example.datn_trendsetter.Entity.DotGiamGia;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import com.example.datn_trendsetter.Entity.PhieuGiamGiaChiTiet;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaChiTietRepository;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhieuGiamGiaService {
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PhieuGiamGiaChiTietRepository phieuGiamGiaChiTietRepository;

    @Transactional
    public Page<PhieuGiamGiaDTO> getAllPhieuGiamGia(Pageable pageable) {
        return phieuGiamGiaRepository.findByDeletedFalse(pageable)
                .map(this::convertToDTO);
    }

    private PhieuGiamGiaDTO convertToDTO(PhieuGiamGia entity) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return new PhieuGiamGiaDTO(
                entity.getId(),
                entity.getMaPhieuGiamGia(),
                entity.getDeleted(),
                entity.getDieuKien(),
                entity.getMoTa(),
                entity.getNgayBatDau() != null ? entity.getNgayBatDau().format(formatter) : null,
                entity.getNgayKetThuc() != null ? entity.getNgayKetThuc().format(formatter) : null,
                entity.getTenPhieuGiamGia(),
                entity.getDonViTinh(),
                entity.getTrangThai(),
                entity.getGiaTriGiam(),
                entity.getSoLuotSuDung()
        );
    }

    public PhieuGiamGia addPhieuGiamGiaForMultipleCustomers(PhieuGiamGiaDTO dto, List<Integer> khachHangIds) {
        List<KhachHang> khachHangs = khachHangRepository.findAllById(khachHangIds);
        if (khachHangs.isEmpty()) {
            throw new RuntimeException("Không tìm thấy khách hàng nào phù hợp");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        PhieuGiamGia phieuGiamGia = new PhieuGiamGia();
        phieuGiamGia.setMaPhieuGiamGia(dto.getMaPhieuGiamGia());
        phieuGiamGia.setTenPhieuGiamGia(dto.getTenPhieuGiamGia());
        phieuGiamGia.setGiaTriGiam(dto.getGiaTriGiam());
        phieuGiamGia.setDonViTinh(dto.getDonViTinh());
        phieuGiamGia.setDieuKien(dto.getDieuKien());
        phieuGiamGia.setMoTa(dto.getMoTa());
        phieuGiamGia.setTrangThai(dto.getTrangThai());
        phieuGiamGia.setSoLuotSuDung(dto.getSoLuotSuDung());
        phieuGiamGia.setDeleted(false);

        // Chuyển đổi từ String sang LocalDate
        if (dto.getNgayBatDau() != null) {
            phieuGiamGia.setNgayBatDau(LocalDate.parse(dto.getNgayBatDau(), formatter));
        }
        if (dto.getNgayKetThuc() != null) {
            phieuGiamGia.setNgayKetThuc(LocalDate.parse(dto.getNgayKetThuc(), formatter));
        }

        phieuGiamGia = phieuGiamGiaRepository.save(phieuGiamGia);

        List<PhieuGiamGiaChiTiet> chiTietList = new ArrayList<>();
        for (KhachHang khachHang : khachHangs) {
            PhieuGiamGiaChiTiet chiTiet = new PhieuGiamGiaChiTiet();
            chiTiet.setKhachHang(khachHang);
            chiTiet.setPhieuGiamGia(phieuGiamGia);
            chiTiet.setSoLuotDaDung(0);
            chiTietList.add(chiTiet);
        }

        phieuGiamGiaChiTietRepository.saveAll(chiTietList);

        return phieuGiamGia;
    }

    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable Integer id) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));

        List<Integer> khachHangIds = phieuGiamGiaChiTietRepository.findByPhieuGiamGiaId(id)
                .stream()
                .map(chiTiet -> chiTiet.getKhachHang().getId())
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("phieuGiamGia", convertToDTO(phieuGiamGia)); // Dùng DTO để tránh lộ thông tin không cần thiết
        response.put("khachHangIds", khachHangIds);

        return ResponseEntity.ok(response);
    }

    @Transactional
    public PhieuGiamGia updatePhieuGiamGia(Integer id, Map<String, Object> requestBody) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));

        Map<String, Object> dto = (Map<String, Object>) requestBody.get("phieuGiamGia");
        List<Integer> khachHangIds = (List<Integer>) requestBody.get("khachHangIds");

        // Cập nhật thông tin phiếu giảm giá
        phieuGiamGia.setMaPhieuGiamGia((String) dto.get("maPhieuGiamGia"));
        phieuGiamGia.setTenPhieuGiamGia((String) dto.get("tenPhieuGiamGia"));
        phieuGiamGia.setGiaTriGiam(((Number) dto.get("giaTriGiam")).floatValue());
        phieuGiamGia.setDonViTinh((String) dto.get("donViTinh"));
        phieuGiamGia.setDieuKien(((Number) dto.get("dieuKien")).floatValue());
        phieuGiamGia.setMoTa((String) dto.get("moTa"));
        phieuGiamGia.setTrangThai((String) dto.get("trangThai"));
        phieuGiamGia.setSoLuotSuDung(((Number) dto.get("soLuotSuDung")).intValue());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (dto.get("ngayBatDau") != null) {
            phieuGiamGia.setNgayBatDau(LocalDate.parse((String) dto.get("ngayBatDau"), formatter));
        }
        if (dto.get("ngayKetThuc") != null) {
            phieuGiamGia.setNgayKetThuc(LocalDate.parse((String) dto.get("ngayKetThuc"), formatter));
        }

        phieuGiamGia = phieuGiamGiaRepository.save(phieuGiamGia);

        // Xóa danh sách khách hàng cũ
        phieuGiamGiaChiTietRepository.deleteByPhieuGiamGia_Id(id);

        // Thêm danh sách khách hàng mới
        List<KhachHang> khachHangs = khachHangRepository.findAllById(khachHangIds);
        List<PhieuGiamGiaChiTiet> chiTietList = new ArrayList<>();
        for (KhachHang khachHang : khachHangs) {
            PhieuGiamGiaChiTiet chiTiet = new PhieuGiamGiaChiTiet();
            chiTiet.setKhachHang(khachHang);
            chiTiet.setPhieuGiamGia(phieuGiamGia);
            chiTiet.setSoLuotDaDung(0);
            chiTietList.add(chiTiet);
        }
        phieuGiamGiaChiTietRepository.saveAll(chiTietList);

        return phieuGiamGia;
    }


    @Transactional
    public void deletePhieuGiamGia(Integer id) {
        PhieuGiamGia entity = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá!"));
        entity.setDeleted(true); // Xóa mềm
        phieuGiamGiaRepository.save(entity);
    }

    @PostConstruct
    public void initPhieuGiamGia() {
        if (phieuGiamGiaRepository.count() == 0) {
            PhieuGiamGia pgg1 = new PhieuGiamGia();
            pgg1.setTenPhieuGiamGia("GIAM50K");
            pgg1.setGiaTriGiam(50000F);
            pgg1.setDieuKien(300000F);
            pgg1.setNgayBatDau(LocalDate.now().minusDays(1));
            pgg1.setNgayKetThuc(LocalDate.now().plusDays(30));
            pgg1.setMoTa("Giảm 50.000đ cho đơn hàng từ 300.000đ");
            pgg1.setTrangThai("Hoạt động");
            pgg1.setNgayTao(LocalDate.now());
            pgg1.setSoLuotSuDung(100);
            pgg1.setSoLuotSuDung(0);
            pgg1.setLoaiApDung("PERCENTAGE");
            pgg1.setIsActive(true);
            pgg1.setDeleted(false);

            PhieuGiamGia pgg2 = new PhieuGiamGia();
            pgg2.setTenPhieuGiamGia("GIAM10%");
            pgg2.setGiaTriGiam(10F);
            pgg2.setDieuKien(500000F);
            pgg2.setNgayBatDau(LocalDate.now().minusDays(1));
            pgg2.setNgayKetThuc(LocalDate.now().plusDays(30));
            pgg2.setMoTa("Giảm 10% cho đơn hàng từ 500.000đ");
            pgg2.setTrangThai("Hoạt động");
            pgg2.setNgayTao(LocalDate.now());
            pgg2.setSoLuotSuDung(200);
            pgg2.setSoLuotSuDung(0);
            pgg2.setLoaiApDung("FIXED_AMOUNT");
            pgg2.setIsActive(true);
            pgg2.setDeleted(false);

            phieuGiamGiaRepository.save(pgg1);
            phieuGiamGiaRepository.save(pgg2);
        }
    }

    public boolean togglePhieuGiamGiaStatus(Integer id) {
        Optional<PhieuGiamGia> optionalPhieuGiamGia = phieuGiamGiaRepository.findById(id);
        if (optionalPhieuGiamGia.isPresent()) {
            PhieuGiamGia phieuGiamGia = optionalPhieuGiamGia.get();

            // Thay đổi trạng thái và đánh dấu xóa mềm
            if ("Đang Hoạt Động".equals(phieuGiamGia.getTrangThai())) {
                phieuGiamGia.setTrangThai("Ngừng Hoạt Động");
                phieuGiamGia.setDeleted(true);
            } else {
                phieuGiamGia.setTrangThai("Đang Hoạt Động");
                phieuGiamGia.setDeleted(false);
            }

            phieuGiamGiaRepository.save(phieuGiamGia);
            return true;
        }
        return false;
    }

}