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
        int soLuongHoaDonThangNay = hoaDonService.countHoaDonThangNay();
        int soLuongHoaDonNgayNay = hoaDonService.countHoaDonNgayNay();
        int soLuongSanPhamThangNay = hoaDonChiTietService.getTongSanPhamBanTrongThang();

        // Lấy dữ liệu thống kê
        Map<String, Integer> totalProductsByDateMonthYear = thongKeService.getTotalProductsByDateMonthYear();
        Map<String, Integer> invoiceData = thongKeService.getInvoiceCountByDateMonthYear();

        List<String> formattedDates = new ArrayList<>(totalProductsByDateMonthYear.keySet());
        List<Integer> salesData = new ArrayList<>(totalProductsByDateMonthYear.values());
        List<Integer> invoiceCounts = new ArrayList<>(invoiceData.values());

        // Đảm bảo danh sách không rỗng trước khi truy cập phần tử
        List<Long> orderStatusPercentages = hoaDonService.getOrderStatusStatistics();
        while (orderStatusPercentages.size() < 8) {
            orderStatusPercentages.add(0L);
        }

        // Thêm vào model dữ liệu thống kê
        model.addAttribute("invoiceData", invoiceCounts.isEmpty() ? Collections.emptyList() : invoiceCounts);
        model.addAttribute("dates", formattedDates.isEmpty() ? Collections.emptyList() : formattedDates);
        model.addAttribute("salesData", salesData.isEmpty() ? Collections.emptyList() : salesData);

        model.addAttribute("processingPercentage", orderStatusPercentages.get(0));
        model.addAttribute("waitingConfirmationPercentage", orderStatusPercentages.get(1));
        model.addAttribute("confirmedPercentage", orderStatusPercentages.get(2));
        model.addAttribute("waitingDeliveryPercentage", orderStatusPercentages.get(3));
        model.addAttribute("paidPercentage", orderStatusPercentages.get(4));
        model.addAttribute("finishedPercentage", orderStatusPercentages.get(5));
        model.addAttribute("cancelledPercentage", orderStatusPercentages.get(6));
        model.addAttribute("failedPercentage", orderStatusPercentages.get(7));

        // Thống kê số lượng sản phẩm bán chạy trong tháng
        List<Object[]> productList = hoaDonChiTietService.getTotalSoldByProductInMonthWithImages();
        model.addAttribute("productsPage", productList.isEmpty() ? Collections.emptyList() : productList);

        // Lấy danh sách sản phẩm sắp hết và hết hàng
        List<SanPhamChiTiet> lowStockProducts = sanPhamChiTietService.findLowStockProducts();
        model.addAttribute("lowStockProducts", lowStockProducts.isEmpty() ? Collections.emptyList() : lowStockProducts);

        // Thêm các giá trị vào model
        model.addAttribute("doanhThuNgayNay", doanhThuNgayNay);
        model.addAttribute("doanhThuThangNay", doanhThuThangNay);
        model.addAttribute("doanhThuNamNay", doanhThuNamNay);
        model.addAttribute("soLuongSanPhamThangNay", soLuongSanPhamThangNay);
        model.addAttribute("soLuongHoaDonNgayNay", soLuongHoaDonNgayNay);
        model.addAttribute("soLuongHoaDonThangNay", soLuongHoaDonThangNay);

        return "Admin/ThongKe/hien-thi";
    }
}
