package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.SanPhamChiTietViewDTO;
import com.example.datn_trendsetter.DTO.StockUpdateRequest;
import com.example.datn_trendsetter.Entity.SanPham;
import com.example.datn_trendsetter.Entity.SanPhamChiTiet;
import com.example.datn_trendsetter.Repository.SanPhamChiTietRepository;
import com.example.datn_trendsetter.Repository.SanPhamRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SanPhamChiTietService {
    @Autowired
    SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    SanPhamRepository sanPhamRepository;

    public List<SanPhamChiTiet> findLowStockProducts() {
        // Lấy tất cả sản phẩm chi tiết có số lượng <= 10 và có trạng thái "Đang Hoạt Động"
        List<SanPhamChiTiet> lowStockProducts = sanPhamChiTietRepository.findLowStockProductsWithActiveParent("Đang Hoạt Động");

        // Lọc để chỉ lấy những sản phẩm chi tiết có hình ảnh (hình ảnh không bị xóa)
        return lowStockProducts.stream()
                .filter(spct -> spct.getHinhAnh() != null && spct.getHinhAnh().stream().anyMatch(ha -> ha.getDeleted() == null || !ha.getDeleted())) // Kiểm tra hình ảnh không null và không bị xóa
                .collect(Collectors.toList());
    }


    public List<SanPhamChiTietViewDTO> getChiTietSanPhamById(Integer idSanPham) {
        List<Object[]> results = sanPhamChiTietRepository.findSanPhamChiTietWithImages(idSanPham);

        // Map để nhóm theo ID sản phẩm chi tiết
        Map<Integer, SanPhamChiTietViewDTO> sanPhamMap = new LinkedHashMap<>();

        // Lặp qua kết quả và xử lý
        for (Object[] row : results) {
            Integer idSanPhamChiTiet = (Integer) row[0];  // ID sản phẩm chi tiết
            String tenSanPham = (String) row[1];  // Tên sản phẩm
            Float gia = (Float) row[2];  // Giá
            String moTa = (String) row[3];  // Mô tả
            String tenKichThuoc = (String) row[4];  // Kích thước
            String tenMauSac = (String) row[5];  // Màu sắc
            String hinhAnh = (String) row[6];  // Hình ảnh
            Integer soLuong = (Integer) row[7];  // Số lượng
            String tenChatLieu = (String) row[8];  // Chất liệu
            String tenThuongHieu = (String) row[9];  // Thương hiệu
            String quocGia = (String) row[10];  // Xuất xứ

            // Kiểm tra nếu bất kỳ trường nào là null, bỏ qua sản phẩm chi tiết này
            if (tenSanPham == null || gia == null || moTa == null || tenKichThuoc == null ||
                    tenMauSac == null || hinhAnh == null || soLuong == null || tenChatLieu == null ||
                    tenThuongHieu == null || quocGia == null) {
                continue;  // Bỏ qua sản phẩm chi tiết này nếu có bất kỳ trường nào là null
            }

            // Nếu sản phẩm chi tiết chưa có trong danh sách, thêm mới
            if (!sanPhamMap.containsKey(idSanPhamChiTiet)) {
                SanPhamChiTietViewDTO newDTO = new SanPhamChiTietViewDTO(
                        idSanPhamChiTiet, tenSanPham, gia, moTa, tenKichThuoc, tenMauSac, hinhAnh, soLuong, tenChatLieu, tenThuongHieu, quocGia
                );
                sanPhamMap.put(idSanPhamChiTiet, newDTO);
            }

            // Lấy DTO hiện tại
            SanPhamChiTietViewDTO dto = sanPhamMap.get(idSanPhamChiTiet);

            // Thêm kích thước và số lượng nếu chưa có
            if (!dto.getSizes().contains(tenKichThuoc)) {
                dto.getSizes().add(tenKichThuoc);
                dto.getSoLuongTheoSize().put(tenKichThuoc, soLuong);
            }

            // Thêm hình ảnh nếu chưa có (hình ảnh không trùng)
            if (hinhAnh != null && !dto.getHinhAnh().contains(hinhAnh)) {
                dto.getHinhAnh().add(hinhAnh);
            }
        }

        return new ArrayList<>(sanPhamMap.values());
    }

    @Transactional
    public Map<String, String> reduceStock(List<StockUpdateRequest> stockUpdates) {
        Map<String, String> results = new HashMap<>();

        for (StockUpdateRequest request : stockUpdates) {
            Integer idSanPhamChiTiet = request.getIdSanPhamChiTiet();
            Integer soLuong = request.getSoLuong();

            int updatedRows = sanPhamChiTietRepository.reduceStock(idSanPhamChiTiet, soLuong);

            if (updatedRows > 0) {
                // ✅ Lấy sản phẩm chi tiết và cập nhật trạng thái
                SanPhamChiTiet spct = sanPhamChiTietRepository.findById(idSanPhamChiTiet).orElse(null);
                if (spct != null) {
                    spct.setTrangThai(spct.getSoLuong() <= 0 ? "Hết Hàng" : "Còn Hàng");
                    sanPhamChiTietRepository.save(spct);
                }

                // ✅ Cập nhật số lượng tổng cho sản phẩm chính
                Integer idSanPham = sanPhamChiTietRepository.findSanPhamIdByChiTietId(idSanPhamChiTiet);
                updateSanPhamStock(idSanPham);

                // ✅ Kiểm tra lại trạng thái sản phẩm chính
                updateSanPhamStatus(idSanPham);

                results.put("Sản phẩm ID " + idSanPhamChiTiet, "Giảm thành công");
            } else {
                results.put("Sản phẩm ID " + idSanPhamChiTiet, "Không đủ hàng hoặc lỗi");
            }
        }

        return results;
    }

    private void updateSanPhamStatus(Integer idSanPham) {
        // Lấy tất cả các sản phẩm chi tiết của sản phẩm chính
        List<SanPhamChiTiet> sanPhamChiTiets = sanPhamChiTietRepository.findBySanPhamId(idSanPham);

        // Tính toán lại số lượng tồn kho của sản phẩm chính
        int totalStock = sanPhamChiTiets.stream().mapToInt(SanPhamChiTiet::getSoLuong).sum();
        updateSanPhamStock(idSanPham, totalStock);

        // Kiểm tra nếu tất cả sản phẩm chi tiết đều hết hàng
        boolean allOutOfStock = sanPhamChiTiets.stream()
                .allMatch(spct -> spct.getSoLuong() <= 0);

        // Cập nhật trạng thái sản phẩm chính
        SanPham sanPham = sanPhamRepository.findById(idSanPham).orElse(null);
        if (sanPham != null) {
            sanPham.setTrangThai(allOutOfStock ? "Không Hoạt Động" : "Đang Hoạt Động");
            sanPhamRepository.save(sanPham);
        }
    }

    private void updateSanPhamStock(Integer idSanPham, int totalStock) {
        // Cập nhật số lượng tổng của sản phẩm chính
        SanPham sanPham = sanPhamRepository.findById(idSanPham).orElse(null);
        if (sanPham != null) {
            sanPham.setSoLuong(totalStock);
            sanPhamRepository.save(sanPham);
        }
    }

    @Transactional
    public void updateSanPhamStock(Integer idSanPham) {
        Integer totalStock = sanPhamChiTietRepository.getTotalStock(idSanPham);
        sanPhamRepository.updateStock(idSanPham, totalStock);
    }

}
