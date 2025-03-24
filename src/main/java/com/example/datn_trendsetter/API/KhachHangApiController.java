package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.DiaChi;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.DiaChiRepository;
import com.example.datn_trendsetter.Service.DiaChiService;
import com.example.datn_trendsetter.Service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/khach-hang")
public class KhachHangApiController {
    @Autowired
    private KhachHangService khachHangService;
    @Autowired
    private DiaChiService diaChiService;


    @GetMapping
    public List<KhachHang> getKhachHang() {
        return khachHangService.getAllKhachHang();
    }

    @GetMapping("/{id}/dia-chi-mac-dinh")
    public ResponseEntity<DiaChi> getDiaChiMacDinh(@PathVariable Integer id) {
        DiaChi diaChiMacDinh = khachHangService.getDiaChiMacDinh(id);
        if (diaChiMacDinh != null) {
            return ResponseEntity.ok(diaChiMacDinh);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/dia-chi/{id}")
    public ResponseEntity<List<DiaChi>> getDiaChi(@PathVariable Integer id) {
        List<DiaChi> diaChi = khachHangService.getAllDiaChi(id);
        if (diaChi != null) {
            return ResponseEntity.ok(diaChi);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/dia-chi/{id}/trang-thai")
    public ResponseEntity<String> updateTrangThai(@PathVariable Integer id) {
        boolean updated = diaChiService.updateTrangThai(id);
        if (updated) {
            return ResponseEntity.ok("Cập nhật trạng thái thành công");
        } else {
            return ResponseEntity.badRequest().body("Không thể cập nhật trạng thái");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addKhachHang(
            @RequestParam("hoTen") String hoTen,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("soDienThoai") String soDienThoai,
            @RequestParam("gioiTinh") Boolean gioiTinh,
            @RequestParam("ngaySinh") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngaySinh,
            @RequestParam("trangThai") String trangThai,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            KhachHang khachHang = new KhachHang();
            khachHang.setHoTen(hoTen);
            khachHang.setUsername(username);
            khachHang.setPassword(password);
            khachHang.setEmail(email);
            khachHang.setSoDienThoai(soDienThoai);
            khachHang.setGioiTinh(gioiTinh);
            khachHang.setNgaySinh(ngaySinh);
            khachHang.setTrangThai(trangThai);

            KhachHang savedKhachHang = khachHangService.addKhachHang(khachHang, file);
            return ResponseEntity.ok(savedKhachHang);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi thêm khách hàng: " + e.getMessage());
        }
    }



    @GetMapping("/check-password")
    public ResponseEntity<Boolean> checkPassword(
            @RequestParam("rawPassword") String rawPassword,
            @RequestParam("id") Integer id) {

        Optional<KhachHang> khachHangOpt = khachHangService.getKhachHangById(id);
        if (khachHangOpt.isPresent()) {
            KhachHang khachHang = khachHangOpt.get();
            boolean isMatch = khachHangService.checkPassword(rawPassword, khachHang.getPassword());
            return ResponseEntity.ok(isMatch);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhachHang> getKhachHangById(@PathVariable Integer id) {
        Optional<KhachHang> khachHangOpt = khachHangService.getKhachHangById(id);
        if (khachHangOpt.isPresent()) {
            return ResponseEntity.ok(khachHangOpt.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<KhachHang> updateKhachHang(
            @PathVariable Integer id,
            @RequestParam("hoTen") String hoTen,
            @RequestParam("username") String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam("email") String email,
            @RequestParam("soDienThoai") String soDienThoai,
            @RequestParam("gioiTinh") Boolean gioiTinh,
            @RequestParam("ngaySinh") String ngaySinh,
            @RequestParam("trangThai") String trangThai,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        KhachHang updatedKhachHang = new KhachHang();
        updatedKhachHang.setHoTen(hoTen);
        updatedKhachHang.setUsername(username);
        updatedKhachHang.setEmail(email);
        updatedKhachHang.setSoDienThoai(soDienThoai);
        updatedKhachHang.setGioiTinh(gioiTinh);
        updatedKhachHang.setNgaySinh(LocalDate.parse(ngaySinh));
        updatedKhachHang.setTrangThai(trangThai);

        // Nếu có mật khẩu mới, thêm vào
        if (password != null && !password.isEmpty()) {
            updatedKhachHang.setPassword(password);
        }

        try {
            KhachHang savedKhachHang = khachHangService.updateKhachHang(id, updatedKhachHang, file);
            if (savedKhachHang != null) {
                return ResponseEntity.ok(savedKhachHang);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}
