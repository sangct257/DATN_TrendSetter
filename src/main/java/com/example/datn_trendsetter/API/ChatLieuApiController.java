package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.ChatLieu;
import com.example.datn_trendsetter.Entity.ThuongHieu;
import com.example.datn_trendsetter.Repository.ChatLieuRepository;
import com.example.datn_trendsetter.Repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat-lieu")
public class ChatLieuApiController {

    @Autowired
    private ChatLieuRepository chatLieuRepository;

    @PostMapping("add")
    public ResponseEntity<String> add(@RequestBody ChatLieu chatLieuRequest) {
        // Kiểm tra xem thương hiệu đã tồn tại chưa
        boolean exists = chatLieuRepository.existsByTenChatLieu(chatLieuRequest.getTenChatLieu());

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên chất liệu đã tồn tại");
        }

        // Nếu chưa tồn tại, tạo mới
        ChatLieu chatLieu = new ChatLieu();
        chatLieu.setMaChatLieu("CL-" + UUID.randomUUID().toString().substring(0, 8)); // Sinh mã ngẫu nhiên
        chatLieu.setTenChatLieu(chatLieuRequest.getTenChatLieu());
        chatLieu.setNgayTao(LocalDate.now());
        chatLieu.setNgaySua(LocalDate.now());
        chatLieu.setNguoiTao("admin");
        chatLieu.setNguoiSua("admin");
        chatLieu.setTrangThai(chatLieuRequest.getTrangThai());
        chatLieu.setDeleted(false);
        chatLieuRepository.save(chatLieu);
        return ResponseEntity.ok("Thêm chất liệu thành công");
    }


    @PostMapping("update")
    public ResponseEntity<String> update(@RequestBody ChatLieu updatedChatLieu) {
        ChatLieu chatLieu = chatLieuRepository.findById(updatedChatLieu.getId()).orElse(null);
        if (chatLieu != null) {
            chatLieu.setMaChatLieu(chatLieu.getMaChatLieu());
            chatLieu.setTenChatLieu(chatLieu.getTenChatLieu());
            chatLieu.setNgayTao(chatLieu.getNgayTao());
            chatLieu.setNgaySua(LocalDate.now());
            chatLieu.setNguoiTao(chatLieu.getNguoiTao());
            chatLieu.setNguoiSua(chatLieu.getNguoiSua());
            chatLieu.setTrangThai(chatLieu.getTrangThai());
            chatLieu.setDeleted(chatLieu.getDeleted());
            chatLieuRepository.save(chatLieu);
            return ResponseEntity.ok("Cập nhật thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy chất liệu");
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
