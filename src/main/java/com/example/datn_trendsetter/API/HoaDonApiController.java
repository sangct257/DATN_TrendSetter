package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.Entity.DotGiamGia;
import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Service.HoaDonService;
import com.example.datn_trendsetter.Service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hoa-don")
public class HoaDonApiController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private ShopService shopService;

    @PostMapping("/create-hoa-don")
    public ResponseEntity<?> createHoaDon(@RequestBody(required = false) HoaDon hoaDon) {
        try {
            if (hoaDon == null) {
                throw new Exception("Dữ liệu hóa đơn không hợp lệ hoặc thiếu.");
            }
            return ResponseEntity.ok(shopService.createHoaDon(hoaDon));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }



    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getHoaDonByTrangThai(@RequestParam(required = false) String trangThai) {
        List<HoaDon> hoaDonList;
        if (trangThai != null && !trangThai.isEmpty()) {
            hoaDonList = hoaDonRepository.findByTrangThai(trangThai, Sort.by(Sort.Direction.DESC, "id"));
        } else {
            hoaDonList = hoaDonRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }

        // Chuyển đổi danh sách hóa đơn sang JSON hợp lệ
        List<Map<String, Object>> jsonResponse = hoaDonList.stream().map(hoaDon -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", hoaDon.getId());
            map.put("maHoaDon", hoaDon.getMaHoaDon());
            map.put("nguoiNhan", hoaDon.getNguoiNhan() != null ? hoaDon.getNguoiNhan() : "Khách lẻ");
            map.put("nguoiTao", hoaDon.getNguoiTao() != null ? hoaDon.getNguoiTao() : "Không có dữ liệu");
            map.put("loaiHoaDon", hoaDon.getLoaiHoaDon() != null ? hoaDon.getLoaiHoaDon() : "Không có dữ liệu");
            map.put("ngayTao", hoaDon.getNgayTao() != null ? hoaDon.getNgayTao().toString() : "Không rõ ngày");
            map.put("phieuGiamGia", hoaDon.getPhieuGiamGia() != null ? Map.of("giaTriGiam", hoaDon.getPhieuGiamGia().getGiaTriGiam()) : null);
            map.put("tongTien", hoaDon.getTongTien() != null ? hoaDon.getTongTien() : 0);
            return map;
        }).toList();

        return ResponseEntity.ok(jsonResponse);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countDotGiamGiaByTrangThai() {
        long dangXuLy = hoaDonRepository.countByTrangThai("Đang Xử Lý");
        long choXacNhan = hoaDonRepository.countByTrangThai("Chờ Xác Nhận");
        long daXacNhan = hoaDonRepository.countByTrangThai("Đã Xác Nhận");
        long choVanChuyen = hoaDonRepository.countByTrangThai("Chờ Vận Chuyển");
        long daThanhToan = hoaDonRepository.countByTrangThai("Đã Thanh Toán");
        long daHoanThanh = hoaDonRepository.countByTrangThai("Đã Hoàn Thành");
        long hoanTra = hoaDonRepository.countByTrangThai("Hoàn Trả");
        long daHuy = hoaDonRepository.countByTrangThai("Đã Hủy");
        long tong = hoaDonRepository.count();

        Map<String, Long> coutMap = Map.of(
                "Đang Xử Lý",dangXuLy,
                "Chờ Xác Nhận",choXacNhan,
                "Đã Xác Nhận",daXacNhan,
                "Chờ Vận Chuyển",choVanChuyen,
                "Đã Thanh Toán",daThanhToan,
                "Đã Hoàn Thành",daHoanThanh,
                "Hoàn Trả",hoanTra,
                "Đã Hủy",daHuy,
                "Tất Cả",tong
        );

        return ResponseEntity.ok().body(coutMap);
    }

}
