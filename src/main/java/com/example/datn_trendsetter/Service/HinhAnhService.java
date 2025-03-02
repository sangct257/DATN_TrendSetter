package com.example.datn_trendsetter.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.datn_trendsetter.Entity.HinhAnh;
import com.example.datn_trendsetter.Entity.SanPhamChiTiet;
import com.example.datn_trendsetter.Repository.HinhAnhRepository;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HinhAnhService {
    @Autowired
    private HinhAnhRepository hinhAnhRepository;

    private final Cloudinary cloudinary;

    public HinhAnhService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public List<HinhAnh> uploadImages(MultipartFile[] files, SanPhamChiTiet sanPhamChiTiet) {
        List<HinhAnh> images = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue; // Bỏ qua tệp rỗng

            try {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                String imageUrl = uploadResult.get("secure_url").toString();
                String publicId = uploadResult.get("public_id").toString();

                HinhAnh hinhAnh = new HinhAnh();
                hinhAnh.setUrlHinhAnh(imageUrl);
                hinhAnh.setPublicId(publicId);
                hinhAnh.setSanPhamChiTiet(sanPhamChiTiet);
                hinhAnh.setNgayTao(LocalDate.now());
                hinhAnh.setTrangThai("ACTIVE");
                hinhAnh.setDeleted(false);

                hinhAnhRepository.save(hinhAnh);
                images.add(hinhAnh);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi upload Cloudinary: " + e.getMessage());
            }
        }
        return images;
    }

    // Phương thức xóa ảnh trên Cloudinary
    public boolean xoaAnhTrenCloudinary(String publicId) {
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equals(result.get("result")) || "not found".equals(result.get("result"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
