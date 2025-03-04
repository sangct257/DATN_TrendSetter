package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.SanPhamChiTiet;
import com.example.datn_trendsetter.Service.HoaDonChiTietService;
import com.example.datn_trendsetter.Service.HoaDonService;
import com.example.datn_trendsetter.Service.SanPhamChiTietService;
import com.example.datn_trendsetter.Service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
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

        // Lấy tháng và năm hiện tại nếu không được truyền từ giao diện
        Float doanhThuNamNay = hoaDonService.getDoanhThuNamNay();
        int soLuongHoaDonThangNay = hoaDonService.countHoaDonThangNay();
        Float doanhThuThangNay = hoaDonService.getDoanhThuThangNay();
        int soLuongHoaDonNgayNay = hoaDonService.countHoaDonNgayNay();
        Float doanhThuNgayNay = hoaDonService.getDoanhThuNgayNay();
        int soLuongSanPhamThangNay = hoaDonChiTietService.getTongSanPhamBanTrongThang();

        List<Object[]> productList = hoaDonChiTietService.getTotalSoldByProductInMonthWithImages(); // Lấy danh sách sản phẩm từ Page
        model.addAttribute("productsPage", productList);


        // Lấy dữ liệu thống kê
        Map<String, Integer> totalProductsByDateMonthYear = thongKeService.getTotalProductsByDateMonthYear();
        Map<String, Integer> invoiceData = thongKeService.getInvoiceCountByDateMonthYear();

        // Chuyển đổi dữ liệu thống kê thành danh sách
        List<String> formattedDates = new ArrayList<>(totalProductsByDateMonthYear.keySet());
        List<Integer> salesData = new ArrayList<>(totalProductsByDateMonthYear.values());
        List<Integer> invoiceCounts = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : invoiceData.entrySet()) {
            invoiceCounts.add(entry.getValue());  // Số lượng hóa đơn
        }

        // Thêm vào model dữ liệu thống kê
        model.addAttribute("invoiceData", invoiceCounts);
        model.addAttribute("dates", formattedDates);
        model.addAttribute("salesData", salesData);

        // Lấy dữ liệu thống kê trạng thái hóa đơn
        List<Long> orderStatusPercentages = hoaDonService.getOrderStatusStatistics();
        model.addAttribute("processingPercentage", orderStatusPercentages.get(0));
        model.addAttribute("waitingConfirmationPercentage", orderStatusPercentages.get(1));
        model.addAttribute("confirmedPercentage", orderStatusPercentages.get(2));
        model.addAttribute("waitingDeliveryPercentage", orderStatusPercentages.get(3));
        model.addAttribute("paidPercentage", orderStatusPercentages.get(4));
        model.addAttribute("finishedPercentage", orderStatusPercentages.get(5));
        model.addAttribute("cancelledPercentage", orderStatusPercentages.get(6));
        model.addAttribute("failedPercentage", orderStatusPercentages.get(7));

        // Lấy danh sách sản phẩm tồn kho thấp
        List<SanPhamChiTiet> lowStockProducts = sanPhamChiTietService.findLowStockProducts();
        model.addAttribute("lowStockProducts", lowStockProducts);

        // Lấy dữ liệu thống kê doanh thu và tăng trưởng
        Map<String, Float> data = thongKeService.getDoanhThuVaTangTruong();

        // Thêm các giá trị vào model
        model.addAttribute("doanhThuNgayNay", doanhThuNgayNay);
        model.addAttribute("doanhThuThangNay", doanhThuThangNay);
        model.addAttribute("doanhThuNamNay", doanhThuNamNay);
        model.addAttribute("soLuongSanPhamThangNay", soLuongSanPhamThangNay);
        model.addAttribute("soLuongHoaDonNgayNay", soLuongHoaDonNgayNay);
        model.addAttribute("soLuongHoaDonThangNay", soLuongHoaDonThangNay);
        model.addAttribute("soLuongSanPhamThangNay", soLuongSanPhamThangNay);
        model.addAttribute("soLuongHoaDonNgayNay", soLuongHoaDonNgayNay);
        model.addAttribute("soLuongHoaDonThangNay", soLuongHoaDonThangNay);

        // Các giá trị tăng trưởng
        model.addAttribute("tangTruongNgay", data.get("tangTruongNgay"));
        model.addAttribute("tangTruongThang", data.get("tangTruongThang"));
        model.addAttribute("tangTruongNam", data.get("tangTruongNam"));

        model.addAttribute("prevTangTruongNgay", data.get("prevTangTruongNgay"));
        model.addAttribute("prevDoanhThuThang", data.get("prevDoanhThuThang"));
        model.addAttribute("prevDoanhThuNam", data.get("prevDoanhThuNam"));


        model.addAttribute("tangTruongSanPham", data.get("tangTruongSanPham"));
        model.addAttribute("tangTruongHoaDonNgay", data.get("tangTruongHoaDonNgay"));
        model.addAttribute("tangTruongHoaDonThang", data.get("tangTruongHoaDonThang"));

        return "Admin/ThongKe/hien-thi";
    }




}
