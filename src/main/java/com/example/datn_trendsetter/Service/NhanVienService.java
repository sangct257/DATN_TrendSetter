package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class NhanVienService {
    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;

    public List<NhanVien> getAllNhanVien() {
        return nhanVienRepository.findAll();
    }

    public NhanVien addNhanVien(NhanVien nhanVien, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(file); // Upload ảnh lên Cloudinary
            nhanVien.setHinhAnh(imageUrl);
        }

        nhanVien.setPassword(passwordEncoder.encode(nhanVien.getPassword())); // Mã hóa mật khẩu
        return nhanVienRepository.save(nhanVien);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Optional<NhanVien> getNhanVienById(Integer id) {
        return nhanVienRepository.findById(id);
    }

    public NhanVien updateNhanVien(Integer id, NhanVien updatedNhanVien, MultipartFile file) throws IOException {
        Optional<NhanVien> existingNhanVienOpt = nhanVienRepository.findById(id);
        if (existingNhanVienOpt.isPresent()) {
            NhanVien existingNhanVien = existingNhanVienOpt.get();

            // Giữ nguyên vai trò cũ
            updatedNhanVien.setVaiTro(existingNhanVien.getVaiTro());

            // Cập nhật các trường thông tin
            existingNhanVien.setHoTen(updatedNhanVien.getHoTen());
            existingNhanVien.setUsername(updatedNhanVien.getUsername());
            existingNhanVien.setEmail(updatedNhanVien.getEmail());
            existingNhanVien.setDiaChi(updatedNhanVien.getDiaChi());
            existingNhanVien.setGioiTinh(updatedNhanVien.getGioiTinh());
            existingNhanVien.setNgaySinh(updatedNhanVien.getNgaySinh());
            existingNhanVien.setTrangThai(updatedNhanVien.getTrangThai());

            // Cập nhật mật khẩu nếu có mật khẩu mới
            if (updatedNhanVien.getPassword() != null && !updatedNhanVien.getPassword().isEmpty()) {
                existingNhanVien.setPassword(passwordEncoder.encode(updatedNhanVien.getPassword())); // Mã hóa mật khẩu mới
            }

            // Cập nhật ảnh nếu có file mới
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(file);
                existingNhanVien.setHinhAnh(imageUrl);
            }

            return nhanVienRepository.save(existingNhanVien); // Lưu lại nhân viên đã cập nhật
        }
        return null; // Trả về null nếu không tìm thấy nhân viên
    }

    public Optional<NhanVien> findByEmail(String email) {
        return nhanVienRepository.findByEmail(email);
    }
}
