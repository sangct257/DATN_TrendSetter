package com.example.datn_trendsetter.Component;

import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Kiểm tra xem có tài khoản admin chưa
        if (nhanVienRepository.findByUsername("admin").isEmpty()) {
            NhanVien admin = new NhanVien();
            admin.setUsername("admin");
            admin.setHoTen("admin");
            admin.setPassword(passwordEncoder.encode("123456")); // Mã hóa mật khẩu
            admin.setVaiTro(NhanVien.Role.ADMIN);
            admin.setEmail("admin@gmail.com");
            admin.setDiaChi("Hà Nội");
            admin.setGioiTinh(true);
            admin.setNgaySinh(null);
            admin.setTrangThai("Đang Hoạt Động");
            admin.setHinhAnh(null);

            nhanVienRepository.save(admin);
            System.out.println("Tài khoản ADMIN đã được tạo.");
        } else {
            System.out.println("Tài khoản ADMIN đã tồn tại.");
        }
    }
}
