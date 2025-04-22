package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.ChatLieu;
import com.example.datn_trendsetter.Entity.DanhMuc;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Entity.ThuongHieu;
import com.example.datn_trendsetter.Repository.DanhMucRepository;
import com.example.datn_trendsetter.Repository.ThuongHieuRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/danh-muc")
public class DanhMucApiController {

    @Autowired
    private DanhMucRepository danhMucRepository;

    @PostMapping("add")
    public ResponseEntity<?> add(@RequestBody DanhMuc danhMucRequest, HttpSession session) {
        try {
            // Kiểm tra tên danh mục đã tồn tại
            boolean exists = danhMucRepository.existsByTenDanhMuc(danhMucRequest.getTenDanhMuc());
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Tên danh mục đã tồn tại"
                ));
            }

            if (danhMucRequest.getTenDanhMuc().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Tên danh mục không được để trống"
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

            // Tạo mới danh mục
            DanhMuc danhMuc = new DanhMuc();
            danhMuc.setMaDanhMuc("DM-" + UUID.randomUUID().toString().substring(0, 8));
            danhMuc.setTenDanhMuc(danhMucRequest.getTenDanhMuc());
            danhMuc.setNgayTao(LocalDate.now());
            danhMuc.setNgaySua(LocalDate.now());
            danhMuc.setNguoiTao(nhanVienSession.getHoTen());
            danhMuc.setNguoiSua(nhanVienSession.getHoTen());
            danhMuc.setDeleted(false);
            danhMuc.setTrangThai("Đang Hoạt Động");

            danhMucRepository.save(danhMuc);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thêm danh mục thành công",
                    "danhMuc", Map.of(
                            "maDanhMuc", danhMuc.getMaDanhMuc(),
                            "tenDanhMuc", danhMuc.getTenDanhMuc(),
                            "trangThai", danhMuc.getTrangThai(),
                            "ngayTao", danhMuc.getNgayTao(),
                            "nguoiTao", danhMuc.getNguoiTao()
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
    public ResponseEntity<?> update(@RequestBody DanhMuc updatedDanhMuc, HttpSession session) {
        try {
            boolean exists = danhMucRepository.existsByTenDanhMuc(updatedDanhMuc.getTenDanhMuc());

            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Tên danh mục đã tồn tại"
                ));
            }

            if (updatedDanhMuc.getTenDanhMuc().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Tên danh mục không được để trống"
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

            // Tìm danh mục theo ID
            DanhMuc danhMuc = danhMucRepository.findById(updatedDanhMuc.getId()).orElse(null);
            if (danhMuc == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy danh mục"
                ));
            }

            // Cập nhật dữ liệu
            danhMuc.setTenDanhMuc(updatedDanhMuc.getTenDanhMuc());
            danhMuc.setTrangThai("Đang Hoạt Động");
            danhMuc.setNguoiSua(nhanVienSession.getHoTen());
            danhMuc.setNgaySua(LocalDate.now());

            danhMucRepository.save(danhMuc);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật danh mục thành công",
                    "danhMuc", Map.of(
                            "id", danhMuc.getId(),
                            "tenDanhMuc", danhMuc.getTenDanhMuc(),
                            "trangThai", danhMuc.getTrangThai(),
                            "ngaySua", danhMuc.getNgaySua(),
                            "nguoiSua", danhMuc.getNguoiSua()
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
    public ResponseEntity<?> delete(@PathVariable Integer id, HttpSession session) {
        try {
            // Lấy nhân viên từ session
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Bạn cần đăng nhập"
                ));
            }

            // Tìm danh mục theo ID
            Optional<DanhMuc> optionalDanhMuc = danhMucRepository.findById(id);
            if (optionalDanhMuc.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy danh mục"
                ));
            }

            // Xóa mềm
            DanhMuc danhMuc = optionalDanhMuc.get();
            danhMuc.setTrangThai("Ngừng Hoạt Động");
            danhMuc.setDeleted(true);
            danhMuc.setNgaySua(LocalDate.now());
            danhMuc.setNguoiSua(nhanVienSession.getHoTen());

            danhMucRepository.save(danhMuc);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa danh mục thành công (đã đánh dấu là deleted)"
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
            Optional<DanhMuc> optionalDanhMuc = danhMucRepository.findById(id);
            if (optionalDanhMuc.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy danh mục"
                ));
            }

            DanhMuc danhMuc = optionalDanhMuc.get();
            String newTrangThai = request.get("trangThai");

            // Cập nhật trạng thái
            danhMuc.setTrangThai(newTrangThai);
            danhMuc.setNgaySua(LocalDate.now());
            danhMuc.setNguoiSua(nhanVienSession.getHoTen());

            danhMucRepository.save(danhMuc);

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

    @GetMapping
    public ResponseEntity<List<DanhMuc>> getAllDanhMuc() {
        return ResponseEntity.ok(danhMucRepository.findAll());
    }
}
