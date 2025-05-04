package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.SanPhamChiTiet;
import com.example.datn_trendsetter.Service.HoaDonChiTietService;
import com.example.datn_trendsetter.Service.HoaDonService;
import com.example.datn_trendsetter.Service.SanPhamChiTietService;
import com.example.datn_trendsetter.Service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class ThongKeControlller {

    @Autowired
    private HoaDonService hoaDonService;
    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;
    @Autowired
    private SanPhamChiTietService sanPhamChiTietService;
    @Autowired
    private ThongKeService thongKeService;

    @RequestMapping("admin/thong-ke")
    public String ThongKe(Model model) {
        // Lấy dữ liệu doanh thu và số lượng hóa đơn
        Float doanhThuNamNay = hoaDonService.getDoanhThuNamNay();
        Float doanhThuThangNay = hoaDonService.getDoanhThuThangNay();
        Float doanhThuNgayNay = hoaDonService.getDoanhThuNgayNay();
        Float doanhSoNamNay = hoaDonService.getDoanhSoNamNay();
        Float doanhSoThangNay = hoaDonService.getDoanhSoThangNay();
        Float doanhSoNgayNay = hoaDonService.getDoanhSoNgayNay();
        int soLuongHoaDonThangNay = hoaDonService.countHoaDonThangNay();
        int soLuongHoaDonNgayNay = hoaDonService.countHoaDonNgayNay();
        int soLuongSanPhamThangNay = hoaDonChiTietService.getTongSanPhamBanTrongThang();

        // Lấy dữ liệu thống kê
        Map<String, Integer> totalProductsByDateMonthYear = thongKeService.getTotalProductsByDateMonthYear();
        List<String> formattedDates = new ArrayList<>(totalProductsByDateMonthYear.keySet());
        List<Integer> salesData = new ArrayList<>(totalProductsByDateMonthYear.values());
        Map<String, Integer> invoiceData = thongKeService.getInvoiceCountByDateMonthYear();
        List<Integer> invoiceCounts = new ArrayList<>(invoiceData.values());
        Map<String, Float> totalRevenueByDateMonthYear = thongKeService.getTotalRevenueByDateMonthYear();
        List<Float> revenueData = new ArrayList<>(totalRevenueByDateMonthYear.values());
        // Thêm vào model dữ liệu thống kê
        model.addAttribute("invoiceData", invoiceCounts.isEmpty() ? Collections.emptyList() : invoiceCounts);
        model.addAttribute("dates", formattedDates.isEmpty() ? Collections.emptyList() : formattedDates);
        model.addAttribute("salesData", salesData.isEmpty() ? Collections.emptyList() : salesData);
        model.addAttribute("revenueData", revenueData.isEmpty() ? Collections.emptyList() : revenueData);

        // Đảm bảo danh sách không rỗng trước khi truy cập phần tử
        // Lấy dữ liệu thống kê
        List<Object> orderStatusStats = hoaDonService.getOrderStatusStatistics();

        // Đảm bảo danh sách không rỗng trước khi truy cập phần tử
        while (orderStatusStats.size() < 16) {
            orderStatusStats.add(0L);  // Thêm các giá trị mặc định nếu thiếu
        }

        // Lấy phần trăm và số lượng
        model.addAttribute("processingPercentage", orderStatusStats.get(0));
        model.addAttribute("waitingConfirmationPercentage", orderStatusStats.get(2));
        model.addAttribute("confirmedPercentage", orderStatusStats.get(4));
        model.addAttribute("waitingDeliveryPercentage", orderStatusStats.get(6));
        model.addAttribute("paidPercentage", orderStatusStats.get(8));
        model.addAttribute("finishedPercentage", orderStatusStats.get(10));
        model.addAttribute("cancelledPercentage", orderStatusStats.get(12));
        model.addAttribute("failedPercentage", orderStatusStats.get(14));

        model.addAttribute("processingCount", orderStatusStats.get(1));
        model.addAttribute("waitingConfirmationCount", orderStatusStats.get(3));
        model.addAttribute("confirmedCount", orderStatusStats.get(5));
        model.addAttribute("waitingDeliveryCount", orderStatusStats.get(7));
        model.addAttribute("paidCount", orderStatusStats.get(9));
        model.addAttribute("finishedCount", orderStatusStats.get(11));
        model.addAttribute("cancelledCount", orderStatusStats.get(13));
        model.addAttribute("failedCount", orderStatusStats.get(15));

        // Thống kê số lượng sản phẩm bán chạy trong tháng
        List<Object[]> productList = hoaDonChiTietService.getTotalSoldByProductInMonthWithImages();
        model.addAttribute("productsPage", productList.isEmpty() ? Collections.emptyList() : productList);

        // Lấy danh sách sản phẩm sắp hết và hết hàng
        List<SanPhamChiTiet> lowStockProducts = sanPhamChiTietService.findLowStockProducts();
        model.addAttribute("lowStockProducts", lowStockProducts.isEmpty() ? Collections.emptyList() : lowStockProducts);

        model.addAttribute("doanhThuNgayNay", doanhThuNgayNay);
        model.addAttribute("doanhThuThangNay", doanhThuThangNay);
        model.addAttribute("doanhThuNamNay", doanhThuNamNay);
        // Thêm các giá trị vào model
        model.addAttribute("doanhSoNgayNay", doanhSoNgayNay);
        model.addAttribute("doanhSoThangNay", doanhSoThangNay);
        model.addAttribute("doanhSoNamNay", doanhSoNamNay);
        model.addAttribute("soLuongSanPhamThangNay", soLuongSanPhamThangNay);
        model.addAttribute("soLuongHoaDonNgayNay", soLuongHoaDonNgayNay);
        model.addAttribute("soLuongHoaDonThangNay", soLuongHoaDonThangNay);

        return "Admin/ThongKe/hien-thi";
    }
}
