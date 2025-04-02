package com.example.datn_trendsetter.API;


import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Entity.XuatXu;
import com.example.datn_trendsetter.Repository.XuatXuRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/xuat-xu")
public class XuatXuApiController {

    @Autowired
    private XuatXuRepository xuatXuRepository;

    @PostMapping("add")
    public ResponseEntity<String> add(@RequestBody XuatXu xuatXuRequest, HttpSession session) throws Exception {
        // Kiểm tra xem thương hiệu đã tồn tại chưa
        boolean exists = xuatXuRepository.existsByQuocGia(xuatXuRequest.getQuocGia());

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên quốc gia đã tồn tại");
        }

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
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
        xuatXu.setTrangThai(xuatXuRequest.getTrangThai());

        xuatXuRepository.save(xuatXu);
        return ResponseEntity.ok("Thêm xuất xứ thành công");
    }


    @PutMapping("update")
    public ResponseEntity<String> update(@RequestBody XuatXu updatedXuatXu,HttpSession session) throws Exception {
        XuatXu xuatXu = xuatXuRepository.findById(updatedXuatXu.getId()).orElse(null);
        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
        }
        if (xuatXu != null) {
            xuatXu.setMaXuatXu(xuatXu.getMaXuatXu());
            xuatXu.setQuocGia(updatedXuatXu.getQuocGia());
            xuatXu.setNgayTao(xuatXu.getNgayTao());
            xuatXu.setNgaySua(LocalDate.now());
            xuatXu.setNguoiSua(nhanVienSession.getHoTen());
            xuatXu.setTrangThai(updatedXuatXu.getTrangThai());
            xuatXu.setDeleted(xuatXu.getDeleted());
            xuatXuRepository.save(xuatXu);
            return ResponseEntity.ok("Cập nhật thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy danh mục");
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        Optional<XuatXu> xuatXu = xuatXuRepository.findById(id);
        if (xuatXu.isPresent()) {
            xuatXuRepository.deleteById(id);
            return ResponseEntity.ok("Xóa xuất xứ thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy xuất xứ");
    }

}
