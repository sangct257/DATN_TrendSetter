package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.DTO.ShippingUpdateRequest;
import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Repository.PhuongThucThanhToanRepository;
import com.example.datn_trendsetter.Service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;


@Controller
public class ShopController {

    @Autowired
    private ShopService shopService;


    @GetMapping("admin/sell-counter")
    public String sellCounter(@RequestParam(value = "hoaDonId", required = false) Integer hoaDonId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        shopService.getHoaDonAndProducts(hoaDonId, model, redirectAttributes);
        return "Admin/dashboard"; // Return to the page where the form is rendered
    }

    @PostMapping("create-hoa-don")
    public String createHoaDon(HoaDon hoaDon, RedirectAttributes redirectAttributes) {
        return shopService.createHoaDon(hoaDon, redirectAttributes);
    }

    @PostMapping("delete/{id}")
    public String deleteHoaDon(@PathVariable("id") Integer hoaDonId) {
        try {
            shopService.deleteHoaDon(hoaDonId);
            return "redirect:/admin/sell-counter"; // Chuyển hướng đến danh sách hóa đơn
        } catch (Exception e) {
            // Thực hiện xử lý khi có lỗi xảy ra, ví dụ: thông báo lỗi cho người dùng
            return "redirect:/admin/sell-counter";
            // Chuyển hướng đến danh sách với thông báo lỗi
        }
    }

    // Xử lý thêm sản phẩm vào hóa đơn
    @PostMapping("add-product-order")
    public String addProductOrder(@RequestParam("sanPhamChiTietId") Integer sanPhamChiTietId,
                                  @RequestParam("hoaDonId") Integer hoaDonId,
                                  @RequestParam("soLuong") Integer soLuong,
                                  RedirectAttributes redirectAttributes) {
        return shopService.handleProductOrder("add", sanPhamChiTietId, hoaDonId, soLuong, null, redirectAttributes);
    }

    @PostMapping("update-product-order")
    public String updateQuantityOrder(@RequestParam("hoaDonChiTietId") Integer hoaDonChiTietId,
                                      @RequestParam("soLuong") Integer soLuong,
                                      @RequestParam("hoaDonId") Integer hoaDonId,
                                      RedirectAttributes redirectAttributes) {
        return shopService.handleProductOrder("update", null, hoaDonId, soLuong, hoaDonChiTietId, redirectAttributes);
    }

//    @PostMapping("delete-product-order")
//    public String deleteProductOrder(@RequestParam(value = "hoaDonChiTietId", required = false) Integer hoaDonChiTietId,
//                                     @RequestParam(value = "hoaDonId", required = false) Integer hoaDonId,
//                                     RedirectAttributes redirectAttributes) {
//        System.out.println("hoaDonChiTietId: " + hoaDonChiTietId);
//        System.out.println("hoaDonId: " + hoaDonId);
//
//        if (hoaDonChiTietId == null || hoaDonId == null) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Thiếu thông tin hóa đơn chi tiết hoặc hóa đơn.");
//            return "redirect:/admin/sell-counter";
//        }
//        return shopService.handleProductOrder("delete", null, hoaDonId, null, hoaDonChiTietId, redirectAttributes);
//    }


    @PostMapping("/delete-product-order")
    public String deleteProductOrder(@RequestParam("hoaDonChiTietId") Integer idHoaDonChiTiet,
                                     @RequestParam("hoaDonId") Integer idHoaDon,
                                     RedirectAttributes redirectAttributes) {
        return shopService.handleProductOrder("delete", null, idHoaDon, null, idHoaDonChiTiet, redirectAttributes);
    }

    @PostMapping("add-customer")
    public String addCustomerToInvoice(@RequestParam("hoaDonId") Integer hoaDonId,
                                       @RequestParam("khachHangId") Integer khachHangId,
                                       RedirectAttributes redirectAttributes) {
        // Gọi phương thức addCustomerToInvoice trong service để xử lý
        return shopService.addCustomerToInvoice(hoaDonId, khachHangId, redirectAttributes);
    }

    @PostMapping("delete-customer")
    public String deleteCustomerToInvoice(@RequestParam("hoaDonId") Integer hoaDonId,
                                       RedirectAttributes redirectAttributes) {
        // Gọi phương thức addCustomerToInvoice trong service để xử lý
        return shopService.deleteCustomerToInvoice(hoaDonId, redirectAttributes);
    }


    @PostMapping("add-payment-method")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addPaymentMethod(@RequestParam("hoaDonId") Integer hoaDonId,
                                                                @RequestParam("phuongThucThanhToanId") Integer phuongThucThanhToanId) {
        try {
            Map<String, String> response = shopService.updatePaymentMethod(hoaDonId, phuongThucThanhToanId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Có lỗi xảy ra trong hệ thống"));
        }
    }

    @PostMapping("apply-phieu-giam-gia")
    public String applyPhieuGiamGia(@RequestParam("hoaDonId") Integer hoaDonId,
                                    @RequestParam("tenChuongTrinh") String tenChuongTrinh,
                                    RedirectAttributes redirectAttributes) {
        return shopService.applyPhieuGiamGia(hoaDonId, tenChuongTrinh, redirectAttributes);
    }

    @PostMapping("confirm-payment")
    public String confirmPayment(@RequestParam("hoaDonId") Integer hoaDonId,
                                 RedirectAttributes redirectAttributes) {
        return shopService.confirmPayment(hoaDonId, redirectAttributes);
    }



    @PostMapping("update-shipping")
    public String updateShipping(@ModelAttribute ShippingUpdateRequest request,
                                 RedirectAttributes redirectAttributes) {
        return shopService.updateShippingAddress(request, redirectAttributes);
    }



}

