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

    // Tạo mã sản phẩm ngẫu nhiên: SP + 6 số
    private String generateMaSanPham() {
        return "SP" + (100000 + new Random().nextInt(900000));
    }

    @Transactional
    public synchronized ResponseEntity<?> addSanPham(SanPhamDTO sanPhamDTO) {
        try {
            Optional<SanPham> existingSanPhamOpt = sanPhamRepository.findByTenSanPhamAndDanhMucIdAndThuongHieuId(
                    sanPhamDTO.getTenSanPham(),
                    sanPhamDTO.getDanhMucId(),
                    sanPhamDTO.getThuongHieuId()
            );

            if (existingSanPhamOpt.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT) // 409 Conflict
                        .body(Map.of("message", "Sản phẩm đã tồn tại trong danh mục và thương hiệu này!"));
            }

            SanPham sanPham = new SanPham();
            sanPham.setMaSanPham(generateMaSanPham());
            sanPham.setTenSanPham(sanPhamDTO.getTenSanPham());
            sanPham.setSoLuong(sanPhamDTO.getSoLuong());
            sanPham.setMoTa(sanPhamDTO.getMoTa());
            sanPham.setTrangThai("Ngừng Hoạt Động");
            sanPham.setNgayTao(LocalDate.now());
            sanPham.setNgaySua(LocalDate.now());
            sanPham.setNguoiTao(sanPhamDTO.getNguoiTao());
            sanPham.setNguoiSua(sanPhamDTO.getNguoiSua());
            sanPham.setDeleted(false);

            // Kiểm tra xem các đối tượng có tồn tại trước khi set không
            sanPham.setThuongHieu(thuongHieuRepository.findById(sanPhamDTO.getThuongHieuId()).orElseThrow(
                    () -> new RuntimeException("Thương hiệu không tồn tại!")));
            sanPham.setDanhMuc(danhMucRepository.findById(sanPhamDTO.getDanhMucId()).orElseThrow(
                    () -> new RuntimeException("Danh mục không tồn tại!")));
            sanPham.setChatLieu(chatLieuRepository.findById(sanPhamDTO.getChatLieuId()).orElseThrow(
                    () -> new RuntimeException("Chất liệu không tồn tại!")));
            sanPham.setXuatXu(xuatXuRepository.findById(sanPhamDTO.getXuatXuId()).orElseThrow(
                    () -> new RuntimeException("Xuất xứ không tồn tại!")));

            // Lưu sản phẩm vào database
            SanPham savedSanPham = sanPhamRepository.save(sanPham);

            // Trả về ID của sản phẩm mới tạo
            return ResponseEntity.ok(Map.of(
                    "message", "Thêm sản phẩm thành công!",
                    "id", savedSanPham.getId()
            ));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Lỗi dữ liệu! Vui lòng kiểm tra lại thông tin sản phẩm."));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Đã xảy ra lỗi trong quá trình thêm sản phẩm."));
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
            sanPham.setThuongHieu(thuongHieuRepository.findById(sanPhamDTO.getThuongHieuId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thương hiệu không hợp lệ!")));
            sanPham.setDanhMuc(danhMucRepository.findById(sanPhamDTO.getDanhMucId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh mục không hợp lệ!")));
            sanPham.setChatLieu(chatLieuRepository.findById(sanPhamDTO.getChatLieuId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chất liệu không hợp lệ!")));
            sanPham.setXuatXu(xuatXuRepository.findById(sanPhamDTO.getXuatXuId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Xuất xứ không hợp lệ!")));

            return sanPhamRepository.save(sanPham);

        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lỗi dữ liệu! Vui lòng kiểm tra lại thông tin sản phẩm.", e);
        } catch (ResponseStatusException e) {
            throw e; // Giữ nguyên lỗi có sẵn
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi trong quá trình cập nhật sản phẩm.", e);
        }
    }


    public boolean toggleSanPhamStatus(Integer id) {
        Optional<SanPham> optionalSanPham = sanPhamRepository.findById(id);
        if (optionalSanPham.isPresent()) {
            SanPham sanPham = optionalSanPham.get();

            // Thay đổi trạng thái và đánh dấu xóa mềm
            if ("Đang Hoạt Động".equals(sanPham.getTrangThai())) {
                sanPham.setTrangThai("Ngừng Hoạt Động");
                sanPham.setDeleted(true);
            } else {
                sanPham.setTrangThai("Đang Hoạt Động");
                sanPham.setDeleted(false);
            }

            sanPhamRepository.save(sanPham);
            return true;
        }
        return false;
    }
}
