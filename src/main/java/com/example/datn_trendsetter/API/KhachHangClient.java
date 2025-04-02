package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.Service.CloudinaryService;
import com.example.datn_trendsetter.Service.KhachHangService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/khach-hang")
public class KhachHangClient {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/info")
    public ResponseEntity<?> getKhachHangInfo(HttpSession session) {
        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        KhachHang khachHang = (KhachHang) session.getAttribute("userKhachHang");

        if (khachHang == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Chưa đăng nhập"
            ));
        }

        // Trả về thông tin khách hàng
        return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                        "hoTen", khachHang.getHoTen(),
                        "email", khachHang.getEmail(),
                        "soDienThoai", khachHang.getSoDienThoai(),
                        "gioiTinh", khachHang.getGioiTinh(),
                        "ngaySinh", khachHang.getNgaySinh(),
                        "hinhAnh", khachHang.getHinhAnh() != null ? khachHang.getHinhAnh() : "default-avatar.png"
                )
        ));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateThongTin(@RequestParam Map<String, String> request,
                                            @RequestParam(value = "file", required = false) MultipartFile file,
                                            HttpSession session) {
        // Kiểm tra xem khách hàng đã đăng nhập chưa
        KhachHang khachHang = (KhachHang) session.getAttribute("userKhachHang");

        if (khachHang == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Chưa đăng nhập"
            ));
        }

        // Cập nhật thông tin từ request
        khachHang.setHoTen(request.get("hoTen"));
        khachHang.setSoDienThoai(request.get("soDienThoai"));
        khachHang.setGioiTinh(Boolean.parseBoolean(request.get("gioiTinh")));
        khachHang.setNgaySinh(LocalDate.parse(request.get("ngaySinh")));

        // Cập nhật ảnh nếu có file mới
        if (file != null && !file.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(file);
                khachHang.setHinhAnh(imageUrl);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "success", false,
                        "message", "Lỗi khi tải ảnh lên"
                ));
            }
        }

        // Lưu lại thông tin khách hàng
        khachHangRepository.save(khachHang);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật thông tin thành công",
                "user", Map.of(
                        "hoTen", khachHang.getHoTen(),
                        "email", khachHang.getEmail(),
                        "soDienThoai", khachHang.getSoDienThoai(),
                        "gioiTinh", khachHang.getGioiTinh(),
                        "ngaySinh", khachHang.getNgaySinh(),
                        "hinhAnh", khachHang.getHinhAnh()
                )
        ));
    }
}
