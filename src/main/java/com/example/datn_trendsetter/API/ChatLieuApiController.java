package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.ChatLieuRepository;
import com.example.datn_trendsetter.Repository.SanPhamRepository;
import com.example.datn_trendsetter.Repository.ThuongHieuRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/chat-lieu")
public class ChatLieuApiController {

    @Autowired
    private ChatLieuRepository chatLieuRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @PostMapping("add")
    public ResponseEntity<?> add(@RequestBody ChatLieu chatLieuRequest , HttpSession session) {
        try {
            // Kiểm tra xem thương hiệu đã tồn tại chưa
            boolean exists = chatLieuRepository.existsByTenChatLieu(chatLieuRequest.getTenChatLieu());

            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Tên chất liệu đã tồn tại"
                ));
            }

            if (chatLieuRequest.getTenChatLieu().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Tên chất liệu không được để trống"
                ));
            }

            // Lấy nhân viên từ session
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Bạn cần đăng nhập"
                ));
            }

            // Nếu chưa tồn tại, tạo mới
            ChatLieu chatLieu = new ChatLieu();
            chatLieu.setMaChatLieu("CL-" + UUID.randomUUID().toString().substring(0, 8)); // Sinh mã ngẫu nhiên
            chatLieu.setTenChatLieu(chatLieuRequest.getTenChatLieu());
            chatLieu.setNgayTao(LocalDate.now());
            chatLieu.setNgaySua(LocalDate.now());
            chatLieu.setNguoiTao(nhanVienSession.getHoTen());
            chatLieu.setNguoiSua(nhanVienSession.getHoTen());
            chatLieu.setTrangThai("Đang Hoạt Động");
            chatLieu.setDeleted(false);
            chatLieuRepository.save(chatLieu);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thêm danh mục thành công",
                    "chatLieu", Map.of(
                            "maChatLieu", chatLieu.getMaChatLieu(),
                            "tenChatLieu", chatLieu.getTenChatLieu(),
                            "trangThai", chatLieu.getTrangThai(),
                            "ngayTao", chatLieu.getNgayTao(),
                            "nguoiTao", chatLieu.getNguoiTao()
                    )
            ));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi: " + e.getMessage()
            ));
        }
    }


    @PutMapping("update")
    public ResponseEntity<?> update(@RequestBody ChatLieu updatedChatLieu, HttpSession session) {
        try {
            Optional<ChatLieu> optionalChatLieu = chatLieuRepository.findById(updatedChatLieu.getId());
            if (optionalChatLieu.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy chất liệu"
                ));
            }

            if (updatedChatLieu.getTenChatLieu().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Tên chất liệu không được để trống"
                ));
            }

            ChatLieu chatLieu = optionalChatLieu.get();

            // Kiểm tra xem thương hiệu đã tồn tại chưa
            boolean exists = chatLieuRepository.existsByTenChatLieu(updatedChatLieu.getTenChatLieu());

            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "Tên chất liệu đã tồn tại"
                ));
            }

            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Bạn cần đăng nhập"
                ));
            }

            chatLieu.setTenChatLieu(updatedChatLieu.getTenChatLieu());
            chatLieu.setTrangThai("Đang Hoạt Động");
            chatLieu.setNgaySua(LocalDate.now());
            chatLieu.setNguoiSua(nhanVienSession.getHoTen());

            chatLieuRepository.save(chatLieu);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật chất liệu thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi: " + e.getMessage()
            ));
        }
    }


    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id, HttpSession session) {
        try {
            // Lấy nhân viên từ session
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Bạn cần đăng nhập"
                ));
            }

            // Tìm chất liệu theo ID
            Optional<ChatLieu> optionalChatLieu = chatLieuRepository.findById(id);
            if (optionalChatLieu.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy chất liệu"
                ));
            }

            ChatLieu chatLieu = optionalChatLieu.get();

            // Kiểm tra xem chất liệu này có đang được dùng trong sản phẩm không
            boolean isUsed = sanPhamRepository.existsByChatLieu(chatLieu);
            if (isUsed) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Không thể xóa. Chất liệu này đang được sử dụng bởi sản phẩm."
                ));
            }

            // Xóa thẳng nếu không bị ràng buộc
            chatLieuRepository.delete(chatLieu);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa chất liệu thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi: " + e.getMessage()
            ));
        }
    }


    @PutMapping("update-trang-thai/{id}")
    public ResponseEntity<?> updateTrangThai(@PathVariable Integer id, @RequestBody Map<String, String> request, HttpSession session) {
        try {
            // Lấy nhân viên từ session
            NhanVien nhanVienSession = (NhanVien) session.getAttribute("userNhanVien");
            if (nhanVienSession == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Bạn cần đăng nhập"
                ));
            }

            // Tìm chất liệu theo ID
            Optional<ChatLieu> optionalChatLieu = chatLieuRepository.findById(id);
            if (optionalChatLieu.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy chất liệu"
                ));
            }

            ChatLieu chatLieu = optionalChatLieu.get();
            String newTrangThai = request.get("trangThai");

            // Cập nhật trạng thái
            chatLieu.setTrangThai(newTrangThai);
            chatLieu.setNgaySua(LocalDate.now());
            chatLieu.setNguoiSua(nhanVienSession.getHoTen());

            chatLieuRepository.save(chatLieu);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật trạng thái thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi: " + e.getMessage()
            ));
        }
    }

}
