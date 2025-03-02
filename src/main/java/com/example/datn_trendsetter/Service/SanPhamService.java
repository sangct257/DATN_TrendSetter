package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.SanPhamChiTietDTO;
import com.example.datn_trendsetter.DTO.SanPhamDTO;
import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
public class SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @Autowired
    private ChatLieuRepository chatLieuRepository;

    @Autowired
    private XuatXuRepository xuatXuRepository;

    @Autowired
    private MauSacRepository mauSacRepository;

    @Autowired
    private KichThuocRepository kichThuocRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    // Tạo mã sản phẩm ngẫu nhiên: SP + 6 số
    private String generateMaSanPham() {
        return "SP" + (100000 + new Random().nextInt(900000));
    }

    @Transactional
    public synchronized SanPham addSanPham(SanPhamDTO sanPhamDTO) {
        try {
            Optional<SanPham> existingSanPhamOpt = sanPhamRepository.findByTenSanPhamAndDanhMucIdAndThuongHieuId(
                    sanPhamDTO.getTenSanPham(),
                    sanPhamDTO.getDanhMucId(),
                    sanPhamDTO.getThuongHieuId()
            );

            if (existingSanPhamOpt.isPresent()) {
                throw new RuntimeException("Sản phẩm đã tồn tại trong danh mục và thương hiệu này!");
            }

            SanPham sanPham = new SanPham();
            sanPham.setMaSanPham(generateMaSanPham());
            sanPham.setTenSanPham(sanPhamDTO.getTenSanPham());
            sanPham.setSoLuong(sanPhamDTO.getSoLuong());
            sanPham.setMoTa(sanPhamDTO.getMoTa());
            sanPham.setTrangThai("Đang Hoạt Động");
            sanPham.setNgayTao(LocalDate.now());
            sanPham.setNgaySua(LocalDate.now());
            sanPham.setNguoiTao(sanPhamDTO.getNguoiTao());
            sanPham.setNguoiSua(sanPhamDTO.getNguoiSua());
            sanPham.setDeleted(false);

            // Set quan hệ
            sanPham.setThuongHieu(thuongHieuRepository.findById(sanPhamDTO.getThuongHieuId()).orElse(null));
            sanPham.setDanhMuc(danhMucRepository.findById(sanPhamDTO.getDanhMucId()).orElse(null));
            sanPham.setChatLieu(chatLieuRepository.findById(sanPhamDTO.getChatLieuId()).orElse(null));
            sanPham.setXuatXu(xuatXuRepository.findById(sanPhamDTO.getXuatXuId()).orElse(null));
            return sanPhamRepository.save(sanPham);

        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Lỗi dữ liệu! Vui lòng kiểm tra lại thông tin sản phẩm.", e);
        } catch (Exception e) {
            throw new RuntimeException("Đã xảy ra lỗi trong quá trình thêm/cập nhật sản phẩm.", e);
        }
    }


    @Transactional
    public synchronized SanPham updateSanPham(Integer id, SanPhamDTO sanPhamDTO) {
        try {
            SanPham sanPham = sanPhamRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại!"));

            // Kiểm tra trùng lặp sản phẩm
            Optional<SanPham> existingSanPhamOpt = sanPhamRepository.findByTenSanPhamAndDanhMucIdAndThuongHieuId(
                    sanPhamDTO.getTenSanPham(),
                    sanPhamDTO.getDanhMucId(),
                    sanPhamDTO.getThuongHieuId()
            );

            if (existingSanPhamOpt.isPresent() && !existingSanPhamOpt.get().getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Sản phẩm đã tồn tại trong danh mục và thương hiệu này!");
            }

            // Cập nhật thông tin sản phẩm
            sanPham.setTenSanPham(sanPhamDTO.getTenSanPham());
            sanPham.setMoTa(sanPhamDTO.getMoTa());

            if (sanPhamDTO.getSoLuong() != null) {
                sanPham.setSoLuong(sanPhamDTO.getSoLuong());
                sanPham.setTrangThai(sanPhamDTO.getSoLuong() > 0 ? "Đang Hoạt Động" : "Không Hoạt Động");
            }

            sanPham.setNgaySua(LocalDate.now());
            sanPham.setNguoiSua(sanPhamDTO.getNguoiSua());

            // Cập nhật quan hệ
            sanPham.setThuongHieu(thuongHieuRepository.findById(sanPhamDTO.getThuongHieuId()).orElse(null));
            sanPham.setDanhMuc(danhMucRepository.findById(sanPhamDTO.getDanhMucId()).orElse(null));
            sanPham.setChatLieu(chatLieuRepository.findById(sanPhamDTO.getChatLieuId()).orElse(null));
            sanPham.setXuatXu(xuatXuRepository.findById(sanPhamDTO.getXuatXuId()).orElse(null));

            return sanPhamRepository.save(sanPham);

        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lỗi dữ liệu! Vui lòng kiểm tra lại thông tin sản phẩm.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Sản phẩm đã tồn tại trong danh mục và thương hiệu này!");
        }
    }

}
