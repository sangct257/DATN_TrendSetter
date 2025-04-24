package com.example.datn_trendsetter.API;


import com.example.datn_trendsetter.Entity.ChatLieu;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Entity.ThuongHieu;
import com.example.datn_trendsetter.Entity.XuatXu;
import com.example.datn_trendsetter.Repository.SanPhamRepository;
import com.example.datn_trendsetter.Repository.XuatXuRepository;
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
@RequestMapping("/api/xuat-xu")
public class XuatXuApiController {

    @Autowired
    private XuatXuRepository xuatXuRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @PostMapping("add")
    public ResponseEntity<?> add(@RequestBody XuatXu xuatXuRequest, HttpSession session) {
        try {
            // Kiểm tra xem thương hiệu đã tồn tại chưa
            boolean exists = xuatXuRepository.existsByQuocGia(xuatXuRequest.getQuocGia());
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Quốc gia đã tồn tại"
                ));
            }

            if (xuatXuRequest.getQuocGia().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Quốc gia không được để trống"
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
            XuatXu xuatXu= new XuatXu();
            xuatXu.setMaXuatXu("XX-" + UUID.randomUUID().toString().substring(0, 8)); // Sinh mã ngẫu nhiên
            xuatXu.setQuocGia(xuatXuRequest.getQuocGia());
            xuatXu.setNgayTao(LocalDate.now());
            xuatXu.setNgaySua(LocalDate.now());
            xuatXu.setNguoiTao(nhanVienSession.getHoTen());
            xuatXu.setNguoiSua(nhanVienSession.getHoTen());
            xuatXu.setDeleted(false);
            xuatXu.setTrangThai("Đang Hoạt Động");

            xuatXuRepository.save(xuatXu);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thêm xuất xứ thành công",
                    "xuatXu", Map.of(
                            "maXuatXu", xuatXu.getMaXuatXu(),
                            "quocGia", xuatXu.getQuocGia(),
                            "trangThai", xuatXu.getTrangThai(),
                            "ngayTao", xuatXu.getNgayTao(),
                            "nguoiTao", xuatXu.getNguoiTao()
                    )
            ));
        } catch (Exception e)  {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi: " + e.getMessage()
            ));
        }
    }


    @PutMapping("update")
    public ResponseEntity<?> update(@RequestBody XuatXu updatedXuatXu,HttpSession session) {
        try {

            boolean exists = xuatXuRepository.existsByQuocGia(updatedXuatXu.getQuocGia());
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Quốc gia đã tồn tại"
                ));
            }

            if (updatedXuatXu.getQuocGia().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Quốc gia không được để trống"
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

            // Tìm xuất xứ theo ID
            XuatXu xuatXu = xuatXuRepository.findById(updatedXuatXu.getId()).orElse(null);
            if (xuatXu == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy xuất xứ"
                ));
            }

            // Cập nhật dữ liệu
            xuatXu.setQuocGia(updatedXuatXu.getQuocGia());
            xuatXu.setTrangThai("Đang Hoạt Động");
            xuatXu.setNguoiSua(nhanVienSession.getHoTen());
            xuatXu.setNgaySua(LocalDate.now());

            xuatXuRepository.save(xuatXu);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật xuất xứ thành công",
                    "xuatXu", Map.of(
                            "id", xuatXu.getId(),
                            "quocGia", xuatXu.getQuocGia(),
                            "trangThai", xuatXu.getTrangThai(),
                            "ngaySua", xuatXu.getNgaySua(),
                            "nguoiSua", xuatXu.getNguoiSua()
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
            // Kiểm tra đăng nhập
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Bạn cần đăng nhập"
                ));
            }

            // Tìm xuất xứ theo ID
            Optional<XuatXu> optionalXuatXu = xuatXuRepository.findById(id);
            if (optionalXuatXu.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy xuất xứ"
                ));
            }

            XuatXu xuatXu = optionalXuatXu.get();

            // Kiểm tra xem có sản phẩm nào đang dùng xuất xứ này không
            boolean isUsed = sanPhamRepository.existsByXuatXu(xuatXu);
            if (isUsed) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Không thể xóa. Xuất xứ này đang được sử dụng trong sản phẩm."
                ));
            }

            // Xóa thẳng
            xuatXuRepository.delete(xuatXu);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa xuất xứ thành công"
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
            Optional<XuatXu> optionalXuatXu = xuatXuRepository.findById(id);
            if (optionalXuatXu.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy quốc gia"
                ));
            }

            XuatXu xuatXu = optionalXuatXu.get();
            String newTrangThai = request.get("trangThai");

            // Cập nhật trạng thái
            xuatXu.setTrangThai(newTrangThai);
            xuatXu.setNgaySua(LocalDate.now());
            xuatXu.setNguoiSua(nhanVienSession.getHoTen());

            xuatXuRepository.save(xuatXu);

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
