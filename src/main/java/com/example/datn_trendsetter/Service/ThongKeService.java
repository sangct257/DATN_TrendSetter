package com.example.datn_trendsetter.Service;

import com.example.datn_trendsetter.Repository.HoaDonChiTietRepository;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Repository.LichSuThanhToanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
@Service
public class ThongKeService {
    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private LichSuThanhToanRepository lichSuThanhToanRepository;

    // Tính doanh thu trong một khoảng thời gian
    public Float getDoanhThu(LocalDateTime startDate, LocalDateTime endDate) {
        return hoaDonRepository.sumTongTienByNgayTaoBetween(startDate, endDate);
    }

    // Tính tốc độ tăng trưởng giữa hai giá trị
    public float calculateGrowth(float prevValue, float currentValue) {
        if (prevValue == 0) return currentValue == 0 ? 0 : 100;  // Nếu giá trị trước bằng 0 và giá trị hiện tại không phải 0, tăng trưởng 100%
        return ((currentValue - prevValue) / prevValue) * 100;
    }

    // Lấy doanh thu và tốc độ tăng trưởng
    public Map<String, Float> getDoanhThuVaTangTruong() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate firstDayOfThisMonth = today.withDayOfMonth(1);
        LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate firstDayOfThisYear = today.withDayOfYear(1);
        LocalDate firstDayOfLastYear = today.minusYears(1).withDayOfYear(1);

        // Tính doanh thu theo các khoảng thời gian
        Float doanhThuNgayNay = getDoanhThu(today.atStartOfDay(), today.atTime(23, 59));
        Float doanhThuNgayTruoc = getDoanhThu(yesterday.atStartOfDay(), yesterday.atTime(23, 59));
        Float doanhThuThangNay = getDoanhThu(firstDayOfThisMonth.atStartOfDay(), today.atTime(23, 59));
        Float doanhThuThangTruoc = getDoanhThu(firstDayOfLastMonth.atStartOfDay(), firstDayOfThisMonth.minusDays(1).atTime(23, 59));
        Float doanhThuNamNay = getDoanhThu(firstDayOfThisYear.atStartOfDay(), today.atTime(23, 59));
        Float doanhThuNamTruoc = getDoanhThu(firstDayOfLastYear.atStartOfDay(), firstDayOfThisYear.minusDays(1).atTime(23, 59));

        // Đảm bảo không có giá trị null
        doanhThuNgayNay = (doanhThuNgayNay != null) ? doanhThuNgayNay : 0.0f;
        doanhThuNgayTruoc = (doanhThuNgayTruoc != null) ? doanhThuNgayTruoc : 0.0f;

        doanhThuThangNay = (doanhThuThangNay != null) ? doanhThuThangNay : 0.0f;
        doanhThuThangTruoc = (doanhThuThangTruoc != null) ? doanhThuThangTruoc : 0.0f;

        doanhThuNamNay = (doanhThuNamNay != null) ? doanhThuNamNay : 0.0f;
        doanhThuNamTruoc = (doanhThuNamTruoc != null) ? doanhThuNamTruoc : 0.0f;

        // Tính số lượng sản phẩm chi tiết và hóa đơn
        Integer soLuongSanPhamThangNay = getSoLuongSanPhamChiTiet(firstDayOfThisMonth, today);
        Integer soLuongSanPhamThangTruoc = getSoLuongSanPhamChiTiet(firstDayOfLastMonth, firstDayOfThisMonth.minusDays(1));

        Integer soLuongHoaDonNgayNay = getSoLuongHoaDon(today);
        Integer soLuongHoaDonNgayTruoc = getSoLuongHoaDon(yesterday);

        Integer soLuongHoaDonThangNay = getSoLuongHoaDon(firstDayOfThisMonth, today);
        Integer soLuongHoaDonThangTruoc = getSoLuongHoaDon(firstDayOfLastMonth, firstDayOfThisMonth.minusDays(1));

        // Đảm bảo số lượng không null
        soLuongSanPhamThangNay = (soLuongSanPhamThangNay != null) ? soLuongSanPhamThangNay : 0;
        soLuongSanPhamThangTruoc = (soLuongSanPhamThangTruoc != null) ? soLuongSanPhamThangTruoc : 0;

        soLuongHoaDonNgayNay = (soLuongHoaDonNgayNay != null) ? soLuongHoaDonNgayNay : 0;
        soLuongHoaDonNgayTruoc = (soLuongHoaDonNgayTruoc != null) ? soLuongHoaDonNgayTruoc : 0;

        soLuongHoaDonThangNay = (soLuongHoaDonThangNay != null) ? soLuongHoaDonThangNay : 0;
        soLuongHoaDonThangTruoc = (soLuongHoaDonThangTruoc != null) ? soLuongHoaDonThangTruoc : 0;

        // Tính tốc độ tăng trưởng
        Map<String, Float> result = new HashMap<>();
        result.put("doanhThuNgayNay", doanhThuNgayNay);
        result.put("tangTruongNgay", calculateGrowth(doanhThuNgayTruoc, doanhThuNgayNay));
        result.put("doanhThuThangNay", doanhThuThangNay);
        result.put("tangTruongThang", calculateGrowth(doanhThuThangTruoc, doanhThuThangNay));
        result.put("doanhThuNamNay", doanhThuNamNay);
        result.put("tangTruongNam", calculateGrowth(doanhThuNamTruoc, doanhThuNamNay));

        // Thêm dữ liệu tăng trưởng cho sản phẩm và hóa đơn

        result.put("tangTruongSanPham", calculateGrowth(soLuongSanPhamThangTruoc, soLuongSanPhamThangNay));
        result.put("tangTruongHoaDonNgay", calculateGrowth(soLuongHoaDonNgayTruoc, soLuongHoaDonNgayNay));
        result.put("tangTruongHoaDonThang", calculateGrowth(soLuongHoaDonThangTruoc, soLuongHoaDonThangNay));

        return result;
    }


    public Integer getSoLuongSanPhamChiTiet(LocalDate startDate, LocalDate endDate) {
        // Đảm bảo chuyển đổi LocalDate thành LocalDateTime để truy vấn dữ liệu chính xác
        return hoaDonChiTietRepository.countSanPhamChiTietByDateRange(startDate.atStartOfDay(), endDate.atTime(23, 59));
    }

    public Integer getSoLuongHoaDon(LocalDate date) {
        return hoaDonRepository.countHoaDonByNgayTao(date.atStartOfDay(), date.atTime(23, 59));
    }

    public Integer getSoLuongHoaDon(LocalDate startDate, LocalDate endDate) {
        return hoaDonRepository.countHoaDonByNgayTaoBetween(startDate.atStartOfDay(), endDate.atTime(23, 59));
    }

    // Lấy tổng số sản phẩm theo ngày, tháng, năm
    public Map<String, Integer> getTotalProductsByDateMonthYear() {
        List<String> trangThaiHoaDon = List.of( "Đã Hoàn Thành"); // chỉ tính đơn hợp lệ
        List<Object[]> results = hoaDonChiTietRepository.getTotalProductsByDateMonthYear(trangThaiHoaDon);
        Map<String, Integer> totalProductsByDateMonthYear = new LinkedHashMap<>();

        for (Object[] result : results) {
            Integer year = ((Number) result[0]).intValue();
            Integer month = ((Number) result[1]).intValue();
            Integer day = ((Number) result[2]).intValue();
            Integer totalProducts = ((Number) result[3]).intValue();

            String key = String.format("%02d - %02d - %04d", day, month, year);
            totalProductsByDateMonthYear.put(key, totalProducts);
        }
        return totalProductsByDateMonthYear;
    }

    // Lấy tổng số hóa đơn theo ngày, tháng, năm
    public Map<String, Integer> getInvoiceCountByDateMonthYear() {
        List<String> trangThaiList = List.of("Đã Hoàn Thành");
        List<Object[]> results = hoaDonRepository.getInvoiceCountByDateMonthYear(trangThaiList);
        Map<String, Integer> invoiceCountByDateMonthYear = new LinkedHashMap<>();

        for (Object[] result : results) {
            Integer year = ((Number) result[0]).intValue();
            Integer month = ((Number) result[1]).intValue();
            Integer day = ((Number) result[2]).intValue();
            Integer totalInvoices = ((Number) result[3]).intValue();

            String key = String.format("%02d - %02d - %04d", day, month, year);
            invoiceCountByDateMonthYear.put(key, totalInvoices);
        }
        return invoiceCountByDateMonthYear;
    }

    // Lấy tổng doanh thu theo ngày, tháng, năm
    public Map<String, Float> getTotalRevenueByDateMonthYear() {
        List<String> trangThaiHoaDon = List.of("Đã Hoàn Thành");

        List<Object[]> results = hoaDonRepository
                .getTotalRevenueByDateMonthYear(trangThaiHoaDon);

        Map<String, Float> totalRevenueByDateMonthYear = new LinkedHashMap<>();

        for (Object[] result : results) {
            Integer year = ((Number) result[0]).intValue();
            Integer month = ((Number) result[1]).intValue();
            Integer day = ((Number) result[2]).intValue();
            Float totalRevenue = ((Number) result[3]).floatValue();

            String key = String.format("%02d - %02d - %04d", day, month, year);
            totalRevenueByDateMonthYear.put(key, totalRevenue);
        }

        return totalRevenueByDateMonthYear;
    }

}
