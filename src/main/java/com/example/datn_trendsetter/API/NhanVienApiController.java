package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
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
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/nhan-vien")
public class NhanVienApiController {
    @Autowired
    private NhanVienService nhanVienService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @GetMapping
    public List<NhanVien> getNhanVien() {
        return nhanVienService.getAllNhanVien();
    }

    @PostMapping("/add")
    public ResponseEntity<?> addNhanVien(
            @RequestParam("hoTen") String hoTen,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("diaChi") String diaChi,
            @RequestParam("gioiTinh") Boolean gioiTinh,
            @RequestParam("ngaySinh") String ngaySinhStr,
            @RequestParam("trangThai") String trangThai,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Map<String, String> warnings = new HashMap<>();

        // Validate cơ bản
        if (hoTen == null || hoTen.trim().isEmpty()) {
            warnings.put("hoTen", "Vui lòng nhập họ tên.");
        }

        if (username == null || username.trim().isEmpty()) {
            warnings.put("username", "Vui lòng nhập tên đăng nhập.");
        }

        if (password == null || password.length() < 6) {
            warnings.put("password", "Mật khẩu phải từ 6 ký tự trở lên.");
        }

        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            warnings.put("email", "Email không hợp lệ.");
        } else if (nhanVienService.existsByEmail(email)) {
            warnings.put("email", "Email đã được sử dụng.");
        }

        if (diaChi == null || diaChi.trim().isEmpty()) {
            warnings.put("diaChi", "Vui lòng nhập địa chỉ.");
        }

        LocalDate ngaySinh = null;
        try {
            ngaySinh = LocalDate.parse(ngaySinhStr);
            if (ngaySinh.isAfter(LocalDate.now())) {
                warnings.put("ngaySinh", "Ngày sinh không được lớn hơn ngày hiện tại.");
            }
        } catch (DateTimeParseException e) {
            warnings.put("ngaySinh", "Ngày sinh không đúng định dạng (yyyy-MM-dd).");
        }

        if (trangThai == null || (!trangThai.equalsIgnoreCase("Đang Hoạt động") && !trangThai.equalsIgnoreCase("Không Hoạt Động"))) {
            warnings.put("trangThai", "Vui lòng chọn trạng thái hợp lệ.");
        }

        // Check trùng Email
        if (nhanVienRepository.existsByEmail(email) || khachHangRepository.existsByEmail(email)) {
            warnings.put("email", "Email đã được sử dụng!");
        }

        if (!warnings.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "warning");
            response.put("messages", warnings);
            return ResponseEntity.ok(response);
        }

        // Nếu không có lỗi thì save
        try {
            NhanVien nhanVien = new NhanVien();
            nhanVien.setHoTen(hoTen.trim());
            nhanVien.setUsername(username.trim());
            nhanVien.setPassword(password.trim());
            nhanVien.setEmail(email.trim());
            nhanVien.setDiaChi(diaChi.trim());
            nhanVien.setGioiTinh(gioiTinh);
            nhanVien.setNgaySinh(ngaySinh);
            nhanVien.setTrangThai(trangThai.trim());
            nhanVien.setVaiTro(NhanVien.Role.NHANVIEN);

            NhanVien savedNhanVien = nhanVienService.addNhanVien(nhanVien, file);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", savedNhanVien
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Lỗi khi thêm nhân viên: " + e.getMessage()
                    ));
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
    public ResponseEntity<?> updateNhanVien(
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
            HttpSession session
    ) {
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");

        if (nhanVienSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "unauthorized", "message", "Vui lòng đăng nhập."));
        }

        Map<String, String> messages = new HashMap<>();

        // Validate các trường cơ bản
        if (hoTen == null || hoTen.trim().isEmpty()) {
            messages.put("hoTen", "Họ tên không được để trống.");
        }

        if (username == null || username.trim().isEmpty()) {
            messages.put("username", "Username không được để trống.");
        }

        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            messages.put("email", "Email không đúng định dạng.");
        }

        if (password != null && !password.isEmpty() && password.length() < 6) {
            messages.put("password", "Mật khẩu phải có ít nhất 6 ký tự.");
        }

        if (diaChi == null || diaChi.trim().isEmpty()) {
            messages.put("diaChi", "Địa chỉ không được để trống.");
        }

        LocalDate parsedNgaySinh = null;
        try {
            parsedNgaySinh = LocalDate.parse(ngaySinh);
            if (parsedNgaySinh.isAfter(LocalDate.now())) {
                messages.put("ngaySinh", "Ngày sinh không được lớn hơn hiện tại.");
            }
        } catch (DateTimeParseException e) {
            messages.put("ngaySinh", "Ngày sinh không đúng định dạng yyyy-MM-dd.");
        }

        if (!trangThai.equalsIgnoreCase("Đang Hoạt Động") && !trangThai.equalsIgnoreCase("Ngừng Hoạt Động")) {
            messages.put("trangThai", "Trạng thái không hợp lệ.");
        }

        // Kiểm tra email trùng (trừ chính mình)
        Optional<NhanVien> existingEmail = nhanVienService.findByEmail(email);
        if (existingEmail.isPresent() && !existingEmail.get().getId().equals(id)) {
            messages.put("email", "Email đã được sử dụng bởi nhân viên khác.");
        }

        // Kiểm tra email trùng với khách hàng (nếu có)
        Optional<KhachHang> existingCustomerEmail = khachHangRepository.findByEmail(email);
        if (existingCustomerEmail.isPresent()) {
            messages.put("email", "Email này đã được sử dụng bởi khách hàng.");
        }

        // Nếu có lỗi, trả về các thông báo
        if (!messages.isEmpty()) {
            return ResponseEntity.ok(Map.of("status", "warning", "messages", messages));
        }

        // Kiểm tra nếu nhân viên cố gắng thay đổi trạng thái của chính mình
        if (nhanVienSession.getId().equals(id) && !trangThai.equals(nhanVienSession.getTrangThai())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "error", "message", "Không thể thay đổi trạng thái của chính mình."));
        }

        // Tạo object NhanVien để cập nhật
        NhanVien updatedNhanVien = new NhanVien();
        updatedNhanVien.setHoTen(hoTen.trim());
        updatedNhanVien.setUsername(username.trim());
        updatedNhanVien.setEmail(email.trim());
        updatedNhanVien.setDiaChi(diaChi.trim());
        updatedNhanVien.setGioiTinh(gioiTinh);
        updatedNhanVien.setNgaySinh(parsedNgaySinh);
        updatedNhanVien.setTrangThai(trangThai.trim());

        if (password != null && !password.isEmpty()) {
            updatedNhanVien.setPassword(password.trim()); // Cân nhắc mã hóa tại service
        }

        try {
            NhanVien saved = nhanVienService.updateNhanVien(id, updatedNhanVien, file);

            if (saved != null) {
                if (nhanVienSession.getId().equals(saved.getId())) {
                    session.setAttribute("userNhanVien", saved);
                }

                return ResponseEntity.ok(Map.of("status", "success", "data", saved));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Không tìm thấy nhân viên để cập nhật."));
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Lỗi xử lý file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }



}
