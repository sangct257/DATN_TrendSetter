package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.DiaChiDTO;
import com.example.datn_trendsetter.Entity.DiaChi;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.DiaChiRepository;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.Service.DiaChiService;
import com.example.datn_trendsetter.Service.KhachHangService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dia-chi")
public class DiaChiApiController {
    @Autowired
    private DiaChiRepository diaChiRepository;

    @Autowired
    private DiaChiService diaChiService;

    // API lấy danh sách địa chỉ của khách hàng đã đăng nhập
    @GetMapping
    public ResponseEntity<?> getDiaChiKhachHang(HttpSession session) {
        // Kiểm tra đăng nhập
        KhachHang khachHang = (KhachHang) session.getAttribute("userKhachHang");
        if (khachHang == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Chưa đăng nhập"
            ));
        }

        // Lấy danh sách địa chỉ của khách hàng từ database
        List<DiaChi> danhSachDiaChi = diaChiRepository.findByKhachHangId(khachHang.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", danhSachDiaChi
        ));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getDiaChiKhachHangs(HttpSession session) {
        // Kiểm tra đăng nhập
        KhachHang khachHang = (KhachHang) session.getAttribute("userKhachHang");
        if (khachHang == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Chưa đăng nhập"
            ));
        }

        // Lấy danh sách địa chỉ của khách hàng từ database
        List<DiaChi> danhSachDiaChi = diaChiRepository.findByKhachHangId(khachHang.getId());

        // Trả về danh sách địa chỉ kèm theo thông tin khách hàng
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", danhSachDiaChi.stream().map(diaChi -> Map.of(
                        "hoTen", khachHang.getHoTen(), // Lấy thông tin khách hàng từ session
                        "soDienThoai", khachHang.getSoDienThoai(),
                        "email", khachHang.getEmail(),
                        "diaChiCuThe", diaChi.getDiaChiCuThe(),
                        "thanhPho", diaChi.getThanhPho(),
                        "huyen", diaChi.getHuyen(),
                        "phuong", diaChi.getPhuong(),
                        "trangThai", diaChi.getTrangThai()
                )).collect(Collectors.toList())
        ));
    }

    // API thêm mới địa chỉ
    @PostMapping
    public ResponseEntity<?> addDiaChiKhachHang(@RequestBody DiaChi diaChi, HttpSession session) {
        // Kiểm tra đăng nhập
        KhachHang khachHang = (KhachHang) session.getAttribute("userKhachHang");
        if (khachHang == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Chưa đăng nhập"
            ));
        }

        // Set thông tin khách hàng cho địa chỉ
        diaChi.setKhachHang(khachHang);

        // Lưu địa chỉ vào cơ sở dữ liệu
        DiaChi savedDiaChi = diaChiRepository.save(diaChi);

        // Trả về kết quả thành công cùng với địa chỉ vừa thêm
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Địa chỉ đã được thêm thành công",
                "data", savedDiaChi
        ));
    }

    // API cập nhật địa chỉ của khách hàng
    @PutMapping("/client/{id}")
    public ResponseEntity<?> updateDiaChiKhachHang(@PathVariable Integer id, @RequestBody DiaChi diaChi, HttpSession session) {
        // Kiểm tra đăng nhập
        KhachHang khachHang = (KhachHang) session.getAttribute("userKhachHang");
        if (khachHang == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Chưa đăng nhập"
            ));
        }

        // Tìm địa chỉ theo ID và kiểm tra sự tồn tại
        DiaChi existingDiaChi = diaChiRepository.findByIdAndKhachHangId(id, khachHang.getId());
        if (existingDiaChi == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Địa chỉ không tồn tại"
            ));
        }

        // Cập nhật thông tin địa chỉ
        existingDiaChi.setDiaChiCuThe(diaChi.getDiaChiCuThe());
        existingDiaChi.setPhuong(diaChi.getPhuong());
        existingDiaChi.setHuyen(diaChi.getHuyen());
        existingDiaChi.setThanhPho(diaChi.getThanhPho());

        // Lưu địa chỉ đã cập nhật vào cơ sở dữ liệu
        DiaChi updatedDiaChi = diaChiRepository.save(existingDiaChi);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Địa chỉ đã được cập nhật thành công",
                "data", updatedDiaChi
        ));
    }

    // API xóa địa chỉ của khách hàng
    @DeleteMapping("/client/{id}")
    public ResponseEntity<?> deleteDiaChiKhachHang(@PathVariable Integer id, HttpSession session) {
        // Kiểm tra đăng nhập
        KhachHang khachHang = (KhachHang) session.getAttribute("userKhachHang");
        if (khachHang == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Chưa đăng nhập"
            ));
        }

        // Tìm địa chỉ theo ID và kiểm tra sự tồn tại
        DiaChi diaChi = diaChiRepository.findByIdAndKhachHangId(id, khachHang.getId());
        if (diaChi == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Địa chỉ không tồn tại"
            ));
        }

        // Xóa địa chỉ
        diaChiRepository.delete(diaChi);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Địa chỉ đã được xóa thành công"
        ));
    }

    @PutMapping("/client/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(@PathVariable Integer id) {
        boolean updated = diaChiService.updateTrangThai(id);
        if (updated) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật trạng thái thành công"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không thể cập nhật trạng thái"
            ));
        }
    }


    @PostMapping("/add")
    public ResponseEntity<?> addDiaChi(@RequestBody DiaChi diaChi, @RequestParam Integer idKhachHang) {
        try {
            if (idKhachHang == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Khách hàng không được để trống.");
            }

            DiaChi savedDiaChi = diaChiService.addDiaChi(diaChi, idKhachHang);
            return ResponseEntity.ok(savedDiaChi);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi thêm địa chỉ: " + e.getMessage());
        }
    }


    @GetMapping("/{idDiaChi}")
    public ResponseEntity<?> getDiaChiById(@PathVariable Integer idDiaChi) {
        Optional<DiaChi> optionalDiaChi = diaChiRepository.findById(idDiaChi);
        if (optionalDiaChi.isPresent()) {
            return ResponseEntity.ok(optionalDiaChi.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy địa chỉ với ID: " + idDiaChi);
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiaChi(@PathVariable Integer id, @RequestBody DiaChi updatedDiaChi) {
        System.out.println("ID nhận được: " + id); // Debug ID nhận từ request
        Optional<DiaChi> optionalDiaChi = diaChiRepository.findById(id);

        if (!optionalDiaChi.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy địa chỉ với ID: " + id);
        }

        DiaChi diaChi = optionalDiaChi.get();
        diaChi.setDiaChiCuThe(updatedDiaChi.getDiaChiCuThe());
        diaChi.setPhuong(updatedDiaChi.getPhuong());
        diaChi.setHuyen(updatedDiaChi.getHuyen());
        diaChi.setThanhPho(updatedDiaChi.getThanhPho());
        diaChi.setTrangThai(diaChi.getTrangThai());

        diaChiRepository.save(diaChi);
        return ResponseEntity.ok("Cập nhật địa chỉ thành công!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDiaChi(@PathVariable Integer id) {
        try {
            diaChiService.deleteDiaChi(id);
            return ResponseEntity.ok("Xóa địa chỉ thành công!");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy địa chỉ với ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa địa chỉ: " + e.getMessage());
        }
    }

}
