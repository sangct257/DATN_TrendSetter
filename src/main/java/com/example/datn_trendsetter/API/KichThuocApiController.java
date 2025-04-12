package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.KichThuoc;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.KichThuocRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/kich-thuoc")
public class KichThuocApiController {

    @Autowired
    private KichThuocRepository kichThuocRepository;

    @PostMapping("add")
    public ResponseEntity<String> add(@RequestBody KichThuoc kichThuocRequest, HttpSession session) throws Exception {
        // Kiểm tra xem thương hiệu đã tồn tại chưa
        boolean exists = kichThuocRepository.existsByTenKichThuoc(kichThuocRequest.getTenKichThuoc());

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên kích thước đã tồn tại");
        }

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
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
        kichThuoc.setTrangThai(kichThuocRequest.getTrangThai());

        kichThuocRepository.save(kichThuoc);
        return ResponseEntity.ok("Thêm kích thước thành công");
    }


    @PutMapping("update")
    public ResponseEntity<String> update(@RequestBody KichThuoc updatedKichThuoc,HttpSession session) throws Exception {
        KichThuoc kichThuoc = kichThuocRepository.findById(updatedKichThuoc.getId()).orElse(null);

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
        }

        if (kichThuoc != null) {
            kichThuoc.setMaKichThuoc(kichThuoc.getMaKichThuoc());
            kichThuoc.setTenKichThuoc(updatedKichThuoc.getTenKichThuoc());
            kichThuoc.setNgayTao(kichThuoc.getNgayTao());
            kichThuoc.setNgaySua(LocalDate.now());
            kichThuoc.setNguoiSua(nhanVienSession.getHoTen());
            kichThuoc.setTrangThai(kichThuoc.getTrangThai());
            kichThuoc.setDeleted(kichThuoc.getDeleted());
            kichThuocRepository.save(kichThuoc);
            return ResponseEntity.ok("Cập nhật thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy danh mục");
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        Optional<KichThuoc> kichThuoc = kichThuocRepository.findById(id);
        if (kichThuoc.isPresent()) {
            kichThuocRepository.deleteById(id);
            return ResponseEntity.ok("Xóa kích thước thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy kích thước");
    }

}
