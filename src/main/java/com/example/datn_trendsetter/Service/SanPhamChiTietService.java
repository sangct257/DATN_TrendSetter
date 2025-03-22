package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.SanPhamChiTietViewDTO;
import com.example.datn_trendsetter.Entity.SanPhamChiTiet;
import com.example.datn_trendsetter.Repository.SanPhamChiTietRepository;
import com.example.datn_trendsetter.Repository.SanPhamRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SanPhamChiTietService {
    @Autowired
    SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    SanPhamRepository sanPhamRepository;

    public List<SanPhamChiTiet> findLowStockProducts() {
        return sanPhamChiTietRepository.findLowStockProducts(); // soLuong < 10 và deleted = false
    }


    public List<SanPhamChiTietViewDTO> getChiTietSanPhamById(Integer idSanPham) {
        List<Object[]> results = sanPhamChiTietRepository.findSanPhamChiTietWithImages(idSanPham);
        // Map để nhóm theo ID sản phẩm chi tiết
        Map<Integer, SanPhamChiTietViewDTO> sanPhamMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            Integer idSanPhamChiTiet = (Integer) row[0];
            String tenSanPham = (String) row[1];
            Float gia = (Float) row[2];
            String moTa = (String) row[3];
            String tenKichThuoc = (String) row[4];
            String tenMauSac = (String) row[5];
            String hinhAnh = (String) row[6];
            Integer soLuong = (Integer) row[7]; // Lấy số lượng

            // Nếu sản phẩm chưa có trong danh sách, thêm mới
            if (!sanPhamMap.containsKey(idSanPhamChiTiet)) {
                sanPhamMap.put(idSanPhamChiTiet, new SanPhamChiTietViewDTO(idSanPhamChiTiet, tenSanPham, gia, moTa, tenKichThuoc, tenMauSac, hinhAnh, soLuong));
            } else {
                // Nếu đã có, cập nhật danh sách size, số lượng và hình ảnh
                SanPhamChiTietViewDTO dto = sanPhamMap.get(idSanPhamChiTiet);

                // Thêm size nếu chưa có
                if (!dto.getSizes().contains(tenKichThuoc)) {
                    dto.getSizes().add(tenKichThuoc);
                    dto.getSoLuongTheoSize().put(tenKichThuoc, soLuong);
                }

                // Cập nhật hình ảnh nếu chưa có
                if (!dto.getHinhAnh().contains(hinhAnh)) {
                    dto.getHinhAnh().add(hinhAnh);
                }

            }

        }

        return new ArrayList<>(sanPhamMap.values());
    }

    @Transactional
    public boolean reduceStock(Integer idSanPhamChiTiet, Integer soLuong) {
        int updatedRows = sanPhamChiTietRepository.reduceStock(idSanPhamChiTiet, soLuong);

        if (updatedRows > 0) {
            // Lấy sản phẩm chi tiết
            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(idSanPhamChiTiet).orElse(null);
            if (spct != null) {
                // Cập nhật trạng thái dựa vào số lượng tồn kho
                if (spct.getSoLuong() <= 0) {
                    spct.setTrangThai("Hết Hàng");
                } else {
                    spct.setTrangThai("Còn Hàng");
                }
                sanPhamChiTietRepository.save(spct); // Lưu thay đổi
            }

            // Cập nhật số lượng tổng cho sản phẩm chính
            Integer idSanPham = sanPhamChiTietRepository.findSanPhamIdByChiTietId(idSanPhamChiTiet);
            updateSanPhamStock(idSanPham);

            return true;
        }
        return false;
    }


    @Transactional
    public void updateSanPhamStock(Integer idSanPham) {
        Integer totalStock = sanPhamChiTietRepository.getTotalStock(idSanPham);
        sanPhamRepository.updateStock(idSanPham, totalStock);
    }

}
