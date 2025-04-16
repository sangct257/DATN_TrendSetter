package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.DiaChi;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Repository.DiaChiRepository;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Service
public class KhachHangService {
    @Autowired
    private KhachHangRepository khachHangRepository;


    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private DiaChiRepository diaChiRepository;

    public List<KhachHang> getAllKhachHang() {
        return khachHangRepository.findAll();
    }

    public DiaChi getDiaChiMacDinh(Integer idKhachHang) {
        return diaChiRepository.findByKhachHangIdAndTrangThai(idKhachHang, "Mặc Định")
                .orElse(null);
    }

    public List<DiaChi> getAllDiaChi(Integer idKhachHang) {
        return diaChiRepository.findByKhachHang_Id(idKhachHang);
    }

    public KhachHang addKhachHang(KhachHang khachHang, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(file); // Upload ảnh lên Cloudinary
            khachHang.setHinhAnh(imageUrl);
        }

        khachHang.setPassword(passwordEncoder.encode(khachHang.getPassword())); // Mã hóa mật khẩu
        return khachHangRepository.save(khachHang);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Optional<KhachHang> findByResetToken(String token) {
        return khachHangRepository.findByResetToken(token);
    }

    public Optional<KhachHang> getKhachHangById(Integer id) {
        return khachHangRepository.findById(id);
    }

    public KhachHang updateKhachHang(Integer id, KhachHang updatedKhachHang, MultipartFile file) throws IOException {
        Optional<KhachHang> existingKhachHangOpt = khachHangRepository.findById(id);
        if (existingKhachHangOpt.isPresent()) {
            KhachHang existingKhachHang = existingKhachHangOpt.get();

            existingKhachHang.setHoTen(updatedKhachHang.getHoTen());
            existingKhachHang.setUsername(updatedKhachHang.getUsername());
            existingKhachHang.setEmail(updatedKhachHang.getEmail());
            existingKhachHang.setSoDienThoai(updatedKhachHang.getSoDienThoai());
            existingKhachHang.setGioiTinh(updatedKhachHang.getGioiTinh());
            existingKhachHang.setNgaySinh(updatedKhachHang.getNgaySinh());
            existingKhachHang.setTrangThai(updatedKhachHang.getTrangThai());

            if (updatedKhachHang.getPassword() != null && !updatedKhachHang.getPassword().isEmpty()) {
                existingKhachHang.setPassword(passwordEncoder.encode(updatedKhachHang.getPassword()));

                if (updatedKhachHang.getResetToken() != null) {
                    existingKhachHang.setResetToken(null);
                }
            }

            if (file != null && !file.isEmpty()) {


                String imageUrl = cloudinaryService.uploadImage(file);
                existingKhachHang.setHinhAnh(imageUrl);
            }

            if (updatedKhachHang.getResetToken() != null) {
                existingKhachHang.setResetToken(updatedKhachHang.getResetToken());
            }

            return khachHangRepository.save(existingKhachHang);
        }
        throw new RuntimeException("Không tìm thấy khách hàng với ID: " + id);
    }
    public Optional<KhachHang> findByEmail(String email) {
        return khachHangRepository.findByEmail(email);
    }

}
