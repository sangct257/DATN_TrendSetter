package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Entity.DotGiamGia;
import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    public Float getDoanhThuNamNay() {
        return hoaDonRepository.getDoanhThuNamNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }

    public Float getDoanhThuThangNay() {
        return hoaDonRepository.getDoanhThuThangNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }

    public int countHoaDonThangNay() {
        return hoaDonRepository.countHoaDonThangNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }


    public Float getDoanhThuNgayNay() {
        return hoaDonRepository.getDoanhThuNgayNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }

    public int countHoaDonNgayNay() {
        return hoaDonRepository.countHoaDonNgayNay(LocalDateTime.now(),"Đã Hoàn Thành");
    }

    public List<Long> getOrderStatusStatistics() {
        // Truy vấn tất cả hóa đơn
        List<HoaDon> allOrders = hoaDonRepository.findAll();

        // Tổng số đơn hàng
        long totalOrders = allOrders.size();

        // Nếu không có đơn hàng nào, trả về tỷ lệ phần trăm bằng 0 cho tất cả các trạng thái
        if (totalOrders == 0) {
            return List.of(0L, 0L, 0L, 0L, 0L, 0L);
        }

        // Đếm số lượng các trạng thái
        long processingOrders = allOrders.stream().filter(order -> "Đang Xử Lý".equals(order.getTrangThai())).count();
        long waitingConfirmationOrders = allOrders.stream().filter(order -> "Chờ Xác Nhận".equals(order.getTrangThai())).count();
        long confirmedOrders = allOrders.stream().filter(order -> "Đã Xác Nhận".equals(order.getTrangThai())).count();
        long waitingDeliveryOrders = allOrders.stream().filter(order -> "Chờ Vận Chuyển".equals(order.getTrangThai())).count();
        long paidOrders = allOrders.stream().filter(order -> "Đã Thanh Toán".equals(order.getTrangThai())).count();
        long finishedOrders = allOrders.stream().filter(order -> "Đã Hoàn Thành".equals(order.getTrangThai())).count();
        long cancelledOrders = allOrders.stream().filter(order -> "Đã Hủy".equals(order.getTrangThai())).count();
        long failedOrders = allOrders.stream().filter(order -> "Hoàn Trả".equals(order.getTrangThai())).count();

        // Tính tỷ lệ phần trăm
        long processingPercentage = (processingOrders * 100) / totalOrders;
        long waitingConfirmationPercentage = (waitingConfirmationOrders * 100) / totalOrders;
        long confirmedPercentage = (confirmedOrders * 100) / totalOrders;
        long waitingDeliveryPercentage = (waitingDeliveryOrders * 100) / totalOrders;
        long paidPercentage = (paidOrders * 100) / totalOrders;
        long finishedPercentage = (finishedOrders * 100) / totalOrders;
        long cancelledPercentage = (cancelledOrders * 100) / totalOrders;
        long failedPercentage = (failedOrders * 100) / totalOrders;


        // Trả về các tỷ lệ phần trăm
        return List.of(processingPercentage, waitingConfirmationPercentage, confirmedPercentage, waitingDeliveryPercentage
                ,paidPercentage,finishedPercentage,cancelledPercentage,failedPercentage);
    }

}
