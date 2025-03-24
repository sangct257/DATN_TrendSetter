package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.DotGiamGia;
import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HoaDonService {
    private final HoaDonRepository hoaDonRepository;

    public boolean updateLoaiHoaDon(Integer hoaDonId, boolean delivery) {
        System.out.println("Cập nhật hóa đơn: ID = " + hoaDonId + ", Delivery = " + delivery);

        Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(hoaDonId);
        if (hoaDonOpt.isEmpty()) {
            System.out.println("Không tìm thấy hóa đơn với ID: " + hoaDonId);
            return false;
        }

        HoaDon hoaDon = hoaDonOpt.get();
        hoaDon.setLoaiHoaDon(delivery ? "Giao Hàng" : "Tại Quầy");

        if (!delivery) {
            hoaDon.setTenDuong(null);
            hoaDon.setHuyen(null);
            hoaDon.setPhuong(null);
            hoaDon.setThanhPho(null);
        }
        if (hoaDon.getLoaiHoaDon().equals("Giao Hàng")) {
            hoaDon.setLoaiGiaoDich("Trả Sau");
        }

        if (hoaDon.getLoaiHoaDon().equals("Tại Quầy")) {
            hoaDon.setLoaiGiaoDich("Đã Thanh Toán");
        }

        hoaDonRepository.save(hoaDon);
        System.out.println("Đã cập nhật hóa đơn thành công");
        return true;
    }

    public HoaDonService(HoaDonRepository hoaDonRepository) {
        this.hoaDonRepository = hoaDonRepository;
    }

    public Float getDoanhSoNamNay() {
        return hoaDonRepository.getDoanhSoNamNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }

    public Float getDoanhSoThangNay() {
        return hoaDonRepository.getDoanhSoThangNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }


    public Float getDoanhSoNgayNay() {
        return hoaDonRepository.getDoanhSoNgayNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }

    public Float getDoanhThuNamNay() {
        return hoaDonRepository.getDoanhThuNamNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }

    public Float getDoanhThuThangNay() {
        return hoaDonRepository.getDoanhThuThangNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }


    public Float getDoanhThuNgayNay() {
        return hoaDonRepository.getDoanhThuNgayNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }


    public int countHoaDonThangNay() {
        return hoaDonRepository.countHoaDonThangNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }


    public int countHoaDonNgayNay() {
        return hoaDonRepository.countHoaDonNgayNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }

    public List<Long> getOrderStatusStatistics() {
        List<HoaDon> allOrders = hoaDonRepository.findAll();
        long totalOrders = allOrders.size();

        if (totalOrders == 0) {
            return List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
        }

        // Danh sách các trạng thái cần thống kê
        List<String> statusList = List.of(
                "Đang Xử Lý", "Chờ Xác Nhận", "Đã Xác Nhận", "Chờ Vận Chuyển",
                "Đã Thanh Toán", "Đã Hoàn Thành", "Đã Hủy", "Hoàn Trả"
        );

        // Đếm số lượng đơn theo trạng thái
        Map<String, Long> statusCount = allOrders.stream()
                .collect(Collectors.groupingBy(HoaDon::getTrangThai, Collectors.counting()));

        // Tính phần trăm
        return statusList.stream()
                .map(status -> statusCount.getOrDefault(status, 0L) * 100 / totalOrders)
                .collect(Collectors.toList());
    }

}
