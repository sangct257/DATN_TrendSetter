package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.KhachHangDTO;
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
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/khach-hang")
public class KhachHangApiController {
    @Autowired
    private KhachHangService khachHangService;
    @Autowired
    private DiaChiService diaChiService;


    @GetMapping("/list")
    public List<KhachHangDTO> getAllKhachHangDTO() {
        return khachHangService.getAllKhachHang().stream()
                .map(kh -> {
                    KhachHangDTO dto = new KhachHangDTO();
                    dto.setId(kh.getId());
                    dto.setHoTen(kh.getHoTen());
                    dto.setUsername(kh.getUsername());
                    dto.setSoDienThoai(kh.getSoDienThoai());
                    dto.setEmail(kh.getEmail());
                    dto.setGioiTinh(kh.getGioiTinh());
                    dto.setNgaySinh(kh.getNgaySinh());
                    dto.setTrangThai(kh.getTrangThai());
                    dto.setHinhAnh(kh.getHinhAnh());
                    return dto;
                })
                .collect(Collectors.toList());
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
            @RequestParam("ngaySinh") String ngaySinhStr,
            @RequestParam("trangThai") String trangThai,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Map<String, String> warnings = new HashMap<>();

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
        }

        if (soDienThoai == null || !soDienThoai.matches("^(0[1-9][0-9]{8})$")) {
            warnings.put("soDienThoai", "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.");
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


        if (trangThai == null || (!trangThai.equalsIgnoreCase("Đang hoạt động") && !trangThai.equalsIgnoreCase("Ngừng hoạt động"))) {
            warnings.put("trangThai", "Vui lòng chọn trạng thái hợp lệ.");
        }

        if (!warnings.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "warning");
            response.put("messages", warnings);
            return ResponseEntity.ok(response);
        }

        try {
            KhachHang khachHang = new KhachHang();
            khachHang.setHoTen(hoTen.trim());
            khachHang.setUsername(username.trim());
            khachHang.setPassword(password.trim());
            khachHang.setEmail(email.trim());
            khachHang.setSoDienThoai(soDienThoai.trim());
            khachHang.setGioiTinh(gioiTinh);
            khachHang.setNgaySinh(ngaySinh);
            khachHang.setTrangThai(trangThai.trim());

            KhachHang savedKhachHang = khachHangService.addKhachHang(khachHang, file);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", savedKhachHang
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Lỗi khi thêm khách hàng: " + e.getMessage()
                    ));
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
    public ResponseEntity<?> updateKhachHang(
            @PathVariable Integer id,
            @RequestParam("hoTen") String hoTen,
            @RequestParam("username") String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam("email") String email,
            @RequestParam("soDienThoai") String soDienThoai,
            @RequestParam("gioiTinh") Boolean gioiTinh,
            @RequestParam("ngaySinh") String ngaySinh,
            @RequestParam("trangThai") String trangThai,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        try {
            Map<String, String> messages = new HashMap<>();

            if (hoTen == null || hoTen.trim().isEmpty()) {
                messages.put("hoTen", "Họ tên không được để trống.");
            }

            if (username == null || username.trim().isEmpty()) {
                messages.put("username", "Tên đăng nhập không được để trống.");
            }

            if (password != null && password.length() < 6) {
                messages.put("password", "Mật khẩu phải có ít nhất 6 ký tự.");
            }

            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                messages.put("email", "Email không hợp lệ.");
            }

            if (soDienThoai == null || !soDienThoai.matches("^(0[1-9][0-9]{8})$")) {
                messages.put("soDienThoai", "Số điện thoại không hợp lệ. Ví dụ: 0981234567");
            }

            LocalDate parsedNgaySinh = null;
            try {
                parsedNgaySinh = LocalDate.parse(ngaySinh);
                if (parsedNgaySinh.isAfter(LocalDate.now())) {
                    messages.put("ngaySinh", "Ngày sinh không được lớn hơn ngày hiện tại.");
                }
            } catch (DateTimeParseException e) {
                messages.put("ngaySinh", "Ngày sinh không đúng định dạng (yyyy-MM-dd).");
            }

            if (!trangThai.equalsIgnoreCase("Đang Hoạt động") && !trangThai.equalsIgnoreCase("Ngừng Hoạt Động")) {
                messages.put("trangThai", "Trạng thái phải là 'Đang Hoạt động' hoặc 'Ngừng Hoạt Động'.");
            }

            if (!messages.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "warning", "messages", messages));
            }

            KhachHang updatedKhachHang = new KhachHang();
            updatedKhachHang.setHoTen(hoTen.trim());
            updatedKhachHang.setUsername(username.trim());
            updatedKhachHang.setEmail(email.trim());
            updatedKhachHang.setSoDienThoai(soDienThoai.trim());
            updatedKhachHang.setGioiTinh(gioiTinh);
            updatedKhachHang.setNgaySinh(parsedNgaySinh);
            updatedKhachHang.setTrangThai(trangThai.trim());

            if (password != null && !password.trim().isEmpty()) {
                updatedKhachHang.setPassword(password.trim()); // Gợi ý mã hóa tại service
            }

            KhachHang saved = khachHangService.updateKhachHang(id, updatedKhachHang, file);

            if (saved != null) {
                KhachHangDTO dto = new KhachHangDTO(
                        saved.getId(),
                        saved.getHoTen(),
                        saved.getUsername(),
                        saved.getEmail(),
                        saved.getSoDienThoai(),
                        saved.getGioiTinh(),
                        saved.getNgaySinh(),
                        saved.getTrangThai(),
                        saved.getHinhAnh()
                );

                return ResponseEntity.ok(Map.of("status", "success", "data", dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Không tìm thấy khách hàng để cập nhật."));
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
