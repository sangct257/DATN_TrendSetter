package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.DTO.DiaChiDTO;
import com.example.datn_trendsetter.Entity.DiaChi;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Repository.DiaChiRepository;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class DiaChiService {

    @Autowired
    private DiaChiRepository diaChiRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Transactional
    public boolean updateTrangThai(Integer id) {
        Optional<DiaChi> diaChiOptional = diaChiRepository.findById(id);
        if (diaChiOptional.isPresent()) {
            DiaChi diaChi = diaChiOptional.get();

            // Nếu địa chỉ chưa là "Mặc Định", đặt nó thành "Mặc Định" và tất cả địa chỉ khác thành "Không Mặc Định"
            if (!"Mặc Định".equals(diaChi.getTrangThai())) {
                diaChiRepository.updateAllToKhongMacDinh(diaChi.getKhachHang().getId(), "Không Mặc Định");
                diaChi.setTrangThai("Mặc Định");
            } else {
                diaChi.setTrangThai("Không Mặc Định");
            }

            diaChiRepository.save(diaChi);
            return true;
        }
        return false;
    }


    public DiaChi addDiaChi(DiaChi diaChi, Integer idKhachHang) {
        if (idKhachHang == null) {
            throw new IllegalArgumentException("Khách hàng không hợp lệ.");
        }

        // Kiểm tra khách hàng có tồn tại không
        Optional<KhachHang> khachHangOpt = khachHangRepository.findById(idKhachHang);
        if (!khachHangOpt.isPresent()) {
            throw new NoSuchElementException("Lỗi: Khách hàng không tồn tại.");
        }

        // Kiểm tra số lượng địa chỉ của khách hàng
        long soLuongDiaChi = diaChiRepository.countByKhachHangId(idKhachHang);
        if (soLuongDiaChi >= 5) {
            throw new RuntimeException("Mỗi khách hàng chỉ có thể có tối đa 5 địa chỉ.");
        }

        // Gán khách hàng vào địa chỉ
        diaChi.setKhachHang(khachHangOpt.get());

        // Lưu vào cơ sở dữ liệu
        return diaChiRepository.save(diaChi);
    }

    @Transactional
    public void deleteDiaChi(Integer id) {
        Optional<DiaChi> diaChiOptional = diaChiRepository.findById(id);
        if (!diaChiOptional.isPresent()) {
            throw new NoSuchElementException("Không tìm thấy địa chỉ với ID: " + id);
        }
        diaChiRepository.deleteById(id);
    }

}