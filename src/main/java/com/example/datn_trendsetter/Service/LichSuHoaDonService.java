package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Entity.HoaDonChiTiet;
import com.example.datn_trendsetter.Entity.LichSuHoaDon;
import com.example.datn_trendsetter.Repository.HoaDonChiTietRepository;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Repository.LichSuHoaDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Service
public class LichSuHoaDonService {
    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    public List<LichSuHoaDon> getAll() {
        return lichSuHoaDonRepository.findAll();
    }

    public void getOrderDetails(Integer hoaDonId, Model model, int page) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);

        // Giới hạn `page` không nhỏ hơn 0
        if (page < 0) page = 0;

        // Phân trang với 5 phần tử mỗi trang
        Pageable pageable = PageRequest.of(page, 5);
        Page<HoaDonChiTiet> hoaDonChiTietPage = hoaDonChiTietRepository.findByHoaDon_Id(hoaDonId, pageable);

        List<LichSuHoaDon> listLichSuHoaDon = lichSuHoaDonRepository.findByHoaDon_Id(hoaDonId);

        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("listLichSuHoaDon", listLichSuHoaDon);
        model.addAttribute("danhSachHoaDonChiTiet", hoaDonChiTietPage.getContent());
        model.addAttribute("pageNumber", page);
        model.addAttribute("totalPages", hoaDonChiTietPage.getTotalPages());

    }
}
