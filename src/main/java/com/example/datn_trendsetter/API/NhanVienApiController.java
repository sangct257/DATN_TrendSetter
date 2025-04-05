package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.NhanVienRepository;
import com.example.datn_trendsetter.Service.CloudinaryService;
import com.example.datn_trendsetter.Service.NhanVienService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/nhan-vien")
public class NhanVienApiController {
    @Autowired
    private NhanVienService nhanVienService;

    @GetMapping
    public List<NhanVien> getNhanVien() {
        return nhanVienService.getAllNhanVien();
    }

    @PostMapping("/add")
    public ResponseEntity<NhanVien> addNhanVien(
            @RequestParam("hoTen") String hoTen,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("diaChi") String diaChi,
            @RequestParam("gioiTinh") Boolean gioiTinh,
            @RequestParam("ngaySinh") String ngaySinh,
            @RequestParam("trangThai") String trangThai,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        NhanVien nhanVien = new NhanVien();
        nhanVien.setHoTen(hoTen);
        nhanVien.setUsername(username);
        nhanVien.setPassword(password);
        nhanVien.setEmail(email);
        nhanVien.setDiaChi(diaChi);
        nhanVien.setGioiTinh(gioiTinh);
        nhanVien.setNgaySinh(LocalDate.parse(ngaySinh));
        nhanVien.setTrangThai(trangThai);
        nhanVien.setVaiTro(NhanVien.Role.NHANVIEN);
        try {
            NhanVien savedNhanVien = nhanVienService.addNhanVien(nhanVien, file);
            return ResponseEntity.ok(savedNhanVien);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/check-password")
    public ResponseEntity<Boolean> checkPassword(
            @RequestParam("rawPassword") String rawPassword,
            @RequestParam("id") Integer id) {

        Optional<NhanVien> nhanVienOpt = nhanVienService.getNhanVienById(id);
        if (nhanVienOpt.isPresent()) {
            NhanVien nhanVien = nhanVienOpt.get();
            boolean isMatch = nhanVienService.checkPassword(rawPassword, nhanVien.getPassword());
            return ResponseEntity.ok(isMatch);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NhanVien> getNhanVienById(@PathVariable Integer id) {
        Optional<NhanVien> nhanVienOpt = nhanVienService.getNhanVienById(id);
        if (nhanVienOpt.isPresent()) {
            return ResponseEntity.ok(nhanVienOpt.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<NhanVien> updateNhanVien(
            @PathVariable Integer id,
            @RequestParam("hoTen") String hoTen,
            @RequestParam("username") String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam("email") String email,
            @RequestParam("diaChi") String diaChi,
            @RequestParam("gioiTinh") Boolean gioiTinh,
            @RequestParam("ngaySinh") String ngaySinh,
            @RequestParam("trangThai") String trangThai,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session) throws Exception {

        // Lấy thông tin nhân viên từ session
        NhanVien nhanVien = (NhanVien) session.getAttribute("userNhanVien");

        // Kiểm tra xem nhân viên đã đăng nhập chưa
        if (nhanVien == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null); // Hoặc có thể trả về message như Map.of("message", "Vui lòng đăng nhập")
        }

        // Khởi tạo đối tượng nhân viên cập nhật
        NhanVien updatedNhanVien = new NhanVien();
        updatedNhanVien.setHoTen(hoTen);
        updatedNhanVien.setUsername(username);
        updatedNhanVien.setEmail(email);
        updatedNhanVien.setDiaChi(diaChi);
        updatedNhanVien.setGioiTinh(gioiTinh);
        updatedNhanVien.setNgaySinh(LocalDate.parse(ngaySinh));
        updatedNhanVien.setTrangThai(trangThai);

        // Nếu có mật khẩu mới, thêm vào
        if (password != null && !password.isEmpty()) {
            updatedNhanVien.setPassword(password);
        }

        try {
            // Cập nhật nhân viên trong cơ sở dữ liệu
            NhanVien savedNhanVien = nhanVienService.updateNhanVien(id, updatedNhanVien, file);

            if (savedNhanVien != null) {
                // Cập nhật lại session nếu nhân viên là chính mình
                if (nhanVien.getId().equals(savedNhanVien.getId())) {
                    session.setAttribute("userNhanVien", savedNhanVien);
                }

                // Trả về nhân viên đã cập nhật thành công
                return ResponseEntity.ok(savedNhanVien);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}
