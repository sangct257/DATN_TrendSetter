package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.ChatLieu;
import com.example.datn_trendsetter.Entity.MauSac;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Entity.ThuongHieu;
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
@RequestMapping("/api/thuong-hieu")
public class ThuongHieuApiController {

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @PostMapping("add")
    public ResponseEntity<?> addThuongHieu(@RequestBody ThuongHieu thuongHieuRequest, HttpSession session){
        try {
            // Kiểm tra xem thương hiệu đã tồn tại chưa
            boolean exists = thuongHieuRepository.existsByTenThuongHieu(thuongHieuRequest.getTenThuongHieu());
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Tên thương hiệu đã tồn tại"
                ));
            }

            if (thuongHieuRequest.getTenThuongHieu().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Tên thương hiệu không được để trống"
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

            // Nếu chưa tồn tại, tạo mới
            ThuongHieu thuongHieu = new ThuongHieu();
            thuongHieu.setMaThuongHieu("TH-" + UUID.randomUUID().toString().substring(0, 8)); // Sinh mã ngẫu nhiên
            thuongHieu.setTenThuongHieu(thuongHieuRequest.getTenThuongHieu());
            thuongHieu.setNgayTao(LocalDate.now());
            thuongHieu.setNgaySua(LocalDate.now());
            thuongHieu.setNguoiTao(nhanVienSession.getHoTen());
            thuongHieu.setNguoiSua(nhanVienSession.getHoTen());
            thuongHieu.setTrangThai("Đang Hoạt Động");
            thuongHieu.setDeleted(false);

            thuongHieuRepository.save(thuongHieu);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thêm thương hiệu thành công",
                    "thuongHieu", Map.of(
                            "maThuongHieu", thuongHieu.getMaThuongHieu(),
                            "tenThuongHieu", thuongHieu.getTenThuongHieu(),
                            "trangThai", thuongHieu.getTrangThai(),
                            "ngayTao", thuongHieu.getNgayTao(),
                            "nguoiTao", thuongHieu.getNguoiTao()
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
    public ResponseEntity<?> updateThuongHieu(@RequestBody ThuongHieu updatedBrand,HttpSession session) {
        try {
            boolean exists = thuongHieuRepository.existsByTenThuongHieu(updatedBrand.getTenThuongHieu());
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Tên thương hiệu đã tồn tại"
                ));
            }

            if (updatedBrand.getTenThuongHieu().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Tên thương hiệu không được để trống"
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

            // Tìm thương hiệu theo ID
            ThuongHieu thuongHieu = thuongHieuRepository.findById(updatedBrand.getId()).orElse(null);
            if (thuongHieu == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy thương hiệu"
                ));
            }

            // Cập nhật dữ liệu
            thuongHieu.setTenThuongHieu(updatedBrand.getTenThuongHieu());
            thuongHieu.setTrangThai("Đang Hoạt Động");
            thuongHieu.setNguoiSua(nhanVienSession.getHoTen());
            thuongHieu.setNgaySua(LocalDate.now());

            thuongHieuRepository.save(thuongHieu);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật thương hiệu thành công",
                    "thuongHieu", Map.of(
                            "id", thuongHieu.getId(),
                            "tenThuongHieu", thuongHieu.getTenThuongHieu(),
                            "trangThai", thuongHieu.getTrangThai(),
                            "ngaySua", thuongHieu.getNgaySua(),
                            "nguoiSua", thuongHieu.getNguoiSua()
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
    public ResponseEntity<?> deleteThuongHieu(@PathVariable Integer id,HttpSession session) {
        try {
            // Lấy nhân viên từ session
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Bạn cần đăng nhập"
                ));
            }


            // Tìm thương hiệu theo ID
            Optional<ThuongHieu> optionalThuongHieu = thuongHieuRepository.findById(id);
            if (optionalThuongHieu.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy thương hiệu"
                ));
            }

            // Xóa mềm
            ThuongHieu thuongHieu = optionalThuongHieu.get();
            thuongHieu.setTrangThai("Ngừng Hoạt Động");
            thuongHieu.setDeleted(true);
            thuongHieu.setNgaySua(LocalDate.now());
            thuongHieu.setNguoiSua(nhanVienSession.getHoTen());

            thuongHieuRepository.save(thuongHieu);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa thương hiệu thành công (đã đánh dấu là deleted)"
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

            // Tìm thương hiệu theo ID
            Optional<ThuongHieu> optionalThuongHieu = thuongHieuRepository.findById(id);
            if (optionalThuongHieu.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy thương hiệu"
                ));
            }

            ThuongHieu thuongHieu = optionalThuongHieu.get();
            String newTrangThai = request.get("trangThai");

            // Cập nhật trạng thái
            thuongHieu.setTrangThai(newTrangThai);
            thuongHieu.setNgaySua(LocalDate.now());
            thuongHieu.setNguoiSua(nhanVienSession.getHoTen());

            thuongHieuRepository.save(thuongHieu);

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
