package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.MauSacRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.MutableAttributeSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/mau-sac")
public class MauSacApiController {
    @Autowired
    private MauSacRepository mauSacRepository;

    @PostMapping("add")
    public ResponseEntity<?> add(@RequestBody MauSac mauSacRequest, HttpSession session) {
        try {
            // Kiểm tra tên danh mục đã tồn tại
            boolean exists = mauSacRepository.existsByTenMauSac(mauSacRequest.getTenMauSac());
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Tên màu sắc đã tồn tại"
                ));
            }

            if (mauSacRequest.getTenMauSac().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Tên màu sắc không được để trống"
                ));
            }

            // Lấy thông tin nhân viên từ session
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Bạn cần đăng nhập"
                ));
            }

            MauSac mauSac = new MauSac();
            mauSac.setMaMauSac("MS-" + UUID.randomUUID().toString().substring(0, 8));
            mauSac.setTenMauSac(mauSacRequest.getTenMauSac());
            mauSac.setNgayTao(LocalDate.now());
            mauSac.setNgaySua(LocalDate.now());
            mauSac.setNguoiTao(nhanVienSession.getHoTen());
            mauSac.setNguoiSua(nhanVienSession.getHoTen());
            mauSac.setDeleted(false);
            mauSac.setTrangThai("Đang Hoạt Động");

            mauSacRepository.save(mauSac);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thêm màu sắc thành công",
                    "mauSac", Map.of(
                            "maMauSac", mauSac.getMaMauSac(),
                            "tenMauSac", mauSac.getTenMauSac(),
                            "trangThai", mauSac.getTrangThai(),
                            "ngayTao", mauSac.getNgayTao(),
                            "nguoiTao", mauSac.getNguoiTao()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi: " + e.getMessage()
            ));
        }
    }



    @PutMapping("update")
    public ResponseEntity<?> update(@RequestBody MauSac updatedMauSac,HttpSession session) {
        try {
            boolean exists = mauSacRepository.existsByTenMauSac(updatedMauSac.getTenMauSac());

            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Tên màu sắc đã tồn tại"
                ));
            }

            if (updatedMauSac.getTenMauSac().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Tên màu sắc không được để trống"
                ));
            }

            // Lấy nhân viên từ session
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Bạn cần đăng nhập"
                ));
            }

            // Tìm màu sắc theo ID
            MauSac mauSac = mauSacRepository.findById(updatedMauSac.getId()).orElse(null);
            if (mauSac == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy màu sắc"
                ));
            }

            // Cập nhật dữ liệu
            mauSac.setTenMauSac(updatedMauSac.getTenMauSac());
            mauSac.setTrangThai("Đang Hoạt Động");
            mauSac.setNguoiSua(nhanVienSession.getHoTen());
            mauSac.setNgaySua(LocalDate.now());

            mauSacRepository.save(mauSac);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật màu sắc thành công",
                    "mauSac", Map.of(
                            "id", mauSac.getId(),
                            "tenMauSac", mauSac.getTenMauSac(),
                            "trangThai", mauSac.getTrangThai(),
                            "ngaySua", mauSac.getNgaySua(),
                            "nguoiSua", mauSac.getNguoiSua()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi: " + e.getMessage()
            ));
        }
    }



    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,HttpSession session) {
        try {
            // Lấy nhân viên từ session
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Bạn cần đăng nhập"
                ));
            }

            // Tìm màu sắc theo ID
            Optional<MauSac> optionalMauSac = mauSacRepository.findById(id);
            if (optionalMauSac.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy màu sắc"
                ));
            }

            // Xóa mềm
            MauSac mauSac = optionalMauSac.get();
            mauSac.setTrangThai("Ngừng Hoạt Động");
            mauSac.setDeleted(true);
            mauSac.setNgaySua(LocalDate.now());
            mauSac.setNguoiSua(nhanVienSession.getHoTen());

            mauSacRepository.save(mauSac);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa màu sắc thành công (đã đánh dấu là deleted)"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi: " + e.getMessage()
            ));
        }
    }

    @PutMapping("update-trang-thai/{id}")
    public ResponseEntity<?> updateTrangThai(@PathVariable Integer id, @RequestBody Map<String, String> request, HttpSession session) {
        try {
            // Lấy nhân viên từ session
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Bạn cần đăng nhập"
                ));
            }

            // Tìm chất liệu theo ID
            Optional<MauSac> optionalMauSac = mauSacRepository.findById(id);
            if (optionalMauSac.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy màu sắc"
                ));
            }

            MauSac mauSac = optionalMauSac.get();
            String newTrangThai = request.get("trangThai");

            // Cập nhật trạng thái
            mauSac.setTrangThai(newTrangThai);
            mauSac.setNgaySua(LocalDate.now());
            mauSac.setNguoiSua(nhanVienSession.getHoTen());

            mauSacRepository.save(mauSac);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật trạng thái thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi: " + e.getMessage()
            ));
        }
    }
}
