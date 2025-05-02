package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.PhieuGiamGiaDTO;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

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
    private HoaDonRepository hoaDonRepository;


    @Transactional
    public List<PhieuGiamGiaDTO> getPhieuGiamGiaByTrangThai(String trangThai) {
        return phieuGiamGiaRepository.findByDeletedFalseAndTrangThai(trangThai, Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PhieuGiamGiaDTO> getAllPhieuGiamGia() {
        return phieuGiamGiaRepository.findByDeletedFalse(Sort.by(Sort.Direction.DESC,"id"))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

    public PhieuGiamGia addPhieuGiamGiaForMultipleCustomers(PhieuGiamGiaDTO dto, HttpSession session) throws Exception {
        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        PhieuGiamGia phieuGiamGia = new PhieuGiamGia();
        phieuGiamGia.setMaPhieuGiamGia(dto.getMaPhieuGiamGia());
        phieuGiamGia.setTenPhieuGiamGia(dto.getTenPhieuGiamGia());
        phieuGiamGia.setGiaTriGiam(dto.getGiaTriGiam());
        phieuGiamGia.setDonViTinh("VND");
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

        phieuGiamGia.setNgayTao(LocalDate.now());
        phieuGiamGia.setNgaySua(LocalDate.now());
        phieuGiamGia.setNguoiTao(nhanVienSession.getHoTen());
        phieuGiamGia.setNguoiSua(nhanVienSession.getHoTen());
        phieuGiamGia = phieuGiamGiaRepository.save(phieuGiamGia);

        return phieuGiamGia;
    }

    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable Integer id) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));


        Map<String, Object> response = new HashMap<>();
        response.put("phieuGiamGia", convertToDTO(phieuGiamGia)); // Dùng DTO để tránh lộ thông tin không cần thiết

        return ResponseEntity.ok(response);
    }

    @Transactional
    public PhieuGiamGia updatePhieuGiamGia(Integer id, Map<String, Object> requestBody ,HttpSession session) throws Exception {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));

        // Kiểm tra nếu phiếu giảm giá đã được dùng trong hóa đơn nào đó
        boolean daSuDung = hoaDonRepository.existsByPhieuGiamGia(phieuGiamGia);
        if (daSuDung) {
            throw new Exception("Phiếu giảm giá đã được sử dụng trong hóa đơn, không thể cập nhật.");
        }

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
        }


        Map<String, Object> dto = (Map<String, Object>) requestBody.get("phieuGiamGia");

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

        phieuGiamGia.setNgaySua(LocalDate.now());
        phieuGiamGia.setNguoiSua(nhanVienSession.getHoTen());
        phieuGiamGia = phieuGiamGiaRepository.save(phieuGiamGia);
        return phieuGiamGia;
    }


    @Transactional
    public void deletePhieuGiamGia(Integer id) {
        PhieuGiamGia entity = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá!"));
        entity.setDeleted(true); // Xóa mềm
        phieuGiamGiaRepository.save(entity);
    }



    public boolean togglePhieuGiamGiaStatus(Integer id) {
        Optional<PhieuGiamGia> optionalPhieuGiamGia = phieuGiamGiaRepository.findById(id);
        if (optionalPhieuGiamGia.isPresent()) {
            PhieuGiamGia phieuGiamGia = optionalPhieuGiamGia.get();

            // Thay đổi trạng thái và đánh dấu xóa mềm
            if ("Đang Hoạt Động".equals(phieuGiamGia.getTrangThai())) {
                phieuGiamGia.setTrangThai("Ngừng Hoạt Động");
                phieuGiamGia.setDeleted(false);
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