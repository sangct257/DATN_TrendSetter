package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.ChatLieu;
import com.example.datn_trendsetter.Entity.DanhMuc;
import com.example.datn_trendsetter.Entity.KichThuoc;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.KichThuocRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/kich-thuoc")
public class KichThuocApiController {

    @Autowired
    private KichThuocRepository kichThuocRepository;

    @PostMapping("add")
    public ResponseEntity<?> add(@RequestBody KichThuoc kichThuocRequest, HttpSession session) {
        try {
            // Kiểm tra xem thương hiệu đã tồn tại chưa
            boolean exists = kichThuocRepository.existsByTenKichThuoc(kichThuocRequest.getTenKichThuoc());
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Tên kích thước đã tồn tại"
                ));
            }

            if (kichThuocRequest.getTenKichThuoc().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Tên kích thước không được để trống"
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

            // Nếu chưa tồn tại, tạo mới
            KichThuoc kichThuoc= new KichThuoc();
            kichThuoc.setMaKichThuoc("KT-" + UUID.randomUUID().toString().substring(0, 8)); // Sinh mã ngẫu nhiên
            kichThuoc.setTenKichThuoc(kichThuocRequest.getTenKichThuoc());
            kichThuoc.setNgayTao(LocalDate.now());
            kichThuoc.setNgaySua(LocalDate.now());
            kichThuoc.setNguoiTao(nhanVienSession.getHoTen());
            kichThuoc.setNguoiSua(nhanVienSession.getHoTen());
            kichThuoc.setDeleted(false);
            kichThuoc.setTrangThai("Đang Hoạt Động");

            kichThuocRepository.save(kichThuoc);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thêm kích thước thành công",
                    "kichThuoc", Map.of(
                            "maKichThuoc", kichThuoc.getMaKichThuoc(),
                            "tenDanhMuc", kichThuoc.getTenKichThuoc(),
                            "trangThai", kichThuoc.getTrangThai(),
                            "ngayTao", kichThuoc.getNgayTao(),
                            "nguoiTao", kichThuoc.getNguoiTao()
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
    public ResponseEntity<?> update(@RequestBody KichThuoc updatedKichThuoc,HttpSession session) {
        try {
            boolean exists = kichThuocRepository.existsByTenKichThuoc(updatedKichThuoc.getTenKichThuoc());
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Tên kích thước đã tồn tại"
                ));
            }

            if (updatedKichThuoc.getTenKichThuoc().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Tên kích thước không được để trống"
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

            KichThuoc kichThuoc = kichThuocRepository.findById(updatedKichThuoc.getId()).orElse(null);
            if (kichThuoc == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy kích thước"
                ));
            }
            // Cập nhật dữ liệu
            kichThuoc.setTenKichThuoc(updatedKichThuoc.getTenKichThuoc());
            kichThuoc.setTrangThai("Đang Hoạt Động");
            kichThuoc.setNguoiSua(nhanVienSession.getHoTen());
            kichThuoc.setNgaySua(LocalDate.now());

            kichThuocRepository.save(kichThuoc);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật danh mục thành công",
                    "kichThuoc", Map.of(
                            "id", kichThuoc.getId(),
                            "tenKichThuoc", kichThuoc.getTenKichThuoc(),
                            "trangThai", kichThuoc.getTrangThai(),
                            "ngaySua", kichThuoc.getNgaySua(),
                            "nguoiSua", kichThuoc.getNguoiSua()
                    )
            ));
        } catch (Exception e){
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

            // Tìm kích thước theo ID
            Optional<KichThuoc> optionalKichThuoc = kichThuocRepository.findById(id);
            if (optionalKichThuoc.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy kích thước"
                ));
            }

            // Xóa mềm
            KichThuoc kichThuoc = optionalKichThuoc.get();
            kichThuoc.setTrangThai("Ngừng Hoạt Động");
            kichThuoc.setDeleted(true);
            kichThuoc.setNgaySua(LocalDate.now());
            kichThuoc.setNguoiSua(nhanVienSession.getHoTen());

            kichThuocRepository.save(kichThuoc);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa kích thước thành công (đã đánh dấu là deleted)"
            ));
        } catch (Exception e){
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
            Optional<KichThuoc> optionalKichThuoc = kichThuocRepository.findById(id);
            if (optionalKichThuoc.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy kích thước"
                ));
            }

            KichThuoc kichThuoc = optionalKichThuoc.get();
            String newTrangThai = request.get("trangThai");

            // Cập nhật trạng thái
            kichThuoc.setTrangThai(newTrangThai);
            kichThuoc.setNgaySua(LocalDate.now());
            kichThuoc.setNguoiSua(nhanVienSession.getHoTen());

            kichThuocRepository.save(kichThuoc);

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
