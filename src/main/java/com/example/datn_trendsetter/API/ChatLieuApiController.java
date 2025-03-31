package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.ChatLieu;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Entity.ThuongHieu;
import com.example.datn_trendsetter.Repository.ChatLieuRepository;
import com.example.datn_trendsetter.Repository.ThuongHieuRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat-lieu")
public class ChatLieuApiController {

    @Autowired
    private ChatLieuRepository chatLieuRepository;

    @PostMapping("add")
    public ResponseEntity<String> add(@RequestBody ChatLieu chatLieuRequest , HttpSession session) throws Exception {
        // Kiểm tra xem thương hiệu đã tồn tại chưa
        boolean exists = chatLieuRepository.existsByTenChatLieu(chatLieuRequest.getTenChatLieu());

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên chất liệu đã tồn tại");
        }

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("user");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
        }

        // Nếu chưa tồn tại, tạo mới
        ChatLieu chatLieu = new ChatLieu();
        chatLieu.setMaChatLieu("CL-" + UUID.randomUUID().toString().substring(0, 8)); // Sinh mã ngẫu nhiên
        chatLieu.setTenChatLieu(chatLieuRequest.getTenChatLieu());
        chatLieu.setNgayTao(LocalDate.now());
        chatLieu.setNgaySua(LocalDate.now());
        chatLieu.setNguoiTao(nhanVienSession.getHoTen());
        chatLieu.setNguoiSua(nhanVienSession.getHoTen());
        chatLieu.setTrangThai(chatLieuRequest.getTrangThai());
        chatLieu.setDeleted(false);
        chatLieuRepository.save(chatLieu);
        return ResponseEntity.ok("Thêm chất liệu thành công");
    }


    @PutMapping("update")
    public ResponseEntity<Map<String, String>> update(@RequestBody ChatLieu updatedChatLieu ,HttpSession session) throws Exception {
        Map<String, String> response = new HashMap<>();

        if (updatedChatLieu.getId() == null) {
            response.put("error", "ID không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        ChatLieu chatLieu = chatLieuRepository.findById(updatedChatLieu.getId()).orElse(null);
        if (chatLieu == null || chatLieu.getDeleted()) {
            response.put("error", "Không tìm thấy chất liệu hoặc đã bị xóa");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Kiểm tra trùng lặp tên chất liệu
        boolean exists = chatLieuRepository.existsByTenChatLieu(updatedChatLieu.getTenChatLieu());
        if (exists && !chatLieu.getTenChatLieu().equalsIgnoreCase(updatedChatLieu.getTenChatLieu())) {
            response.put("error", "Tên chất liệu đã tồn tại");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Lấy nhân viên từ session
        NhanVien nhanVienSession = (NhanVien) session.getAttribute("user");
        if (nhanVienSession == null) {
            throw new Exception("Bạn cần đăng nhập.");
        }

        // Cập nhật thông tin chất liệu
        chatLieu.setTenChatLieu(updatedChatLieu.getTenChatLieu());
        chatLieu.setNgaySua(LocalDate.now());
        chatLieu.setNguoiSua(nhanVienSession.getHoTen());
        chatLieu.setTrangThai(updatedChatLieu.getTrangThai());

        chatLieuRepository.save(chatLieu);

        response.put("message", "Cập nhật thành công");
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        Optional<ChatLieu> chatLieu = chatLieuRepository.findById(id);
        if (chatLieu.isPresent()) {
            chatLieuRepository.deleteById(id);
            return ResponseEntity.ok("Xóa chất liệu thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy chất liệu");
    }

}
