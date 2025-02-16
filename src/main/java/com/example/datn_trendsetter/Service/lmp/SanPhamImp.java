//package com.example.datn_trendsetter.Service.lmp;
//
//import com.example.datn_trendsetter.Entity.HinhAnh;
//import com.example.datn_trendsetter.Entity.SanPhamChiTiet;
//import com.example.datn_trendsetter.Repository.HinhAnhRepository;
//import com.example.datn_trendsetter.Repository.SanPhamChiTietRepository;
//import com.example.datn_trendsetter.Repository.SanPhamRepository;
//import com.example.datn_trendsetter.Service.SanPhamService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class SanPhamImp implements SanPhamService {
//    private final SanPhamRepository sanPhamRepository;
//    private final SanPhamChiTietRepository sanPhamChiTietRepository;
//    private final HinhAnhRepository hinhAnhRepository;
//
//    @Override
//    public Page<SanPhamDTO> getAllSanPhamBasicInfo(int page) {
//        // Cấu hình phân trang với 8 sản phẩm mỗi trang
//        Pageable pageable = PageRequest.of(page, 8);
//
//        // Lấy danh sách sản phẩm theo phân trang
//        return sanPhamRepository.findAll(pageable).map(sanPham -> {
//            // Lấy danh sách chi tiết sản phẩm
//            List<SanPhamChiTiet> sanPhamChiTiets = sanPhamChiTietRepository.findBySanPhamId(sanPham.getId());
//            // Lấy giá của sản phẩm (ví dụ lấy giá đầu tiên trong danh sách chi tiết)
//            Double gia = sanPhamChiTiets.isEmpty() ? 0.0 : sanPhamChiTiets.get(0).getGia();
//
//            // Lấy tất cả hình ảnh của sản phẩm
//            List<HinhAnh> hinhAnhs = hinhAnhRepository.findBySanPhamIdOrderByIdAsc(sanPham.getId());
//            List<String> hinhAnhUrls = hinhAnhs.stream()
//                    .map(HinhAnh::getUrlHinhAnh)
//                    .collect(Collectors.toList());
//
//            // Lấy tất cả tên màu sắc của sản phẩm từ danh sách chi tiết sản phẩm
//            List<String> mauSacList = sanPhamChiTiets.stream()
//                    .map(sanPhamChiTiet -> sanPhamChiTiet.getMauSac().getTenMauSac()) // Lấy tên màu sắc từ MauSac
//                    .collect(Collectors.toList());
//
//            // Trả về DTO với thông tin đầy đủ
//            return SanPhamDTO.builder()
//                    .tenSanPham(sanPham.getTenSanPham())
//                    .gia(gia)
//                    .hinhAnhUrls(hinhAnhUrls)   // Danh sách tất cả các hình ảnh
//                    .mauSacList(mauSacList)     // Danh sách tất cả màu sắc (dưới dạng chuỗi)
//                    .build();
//        });
//    }
//}
