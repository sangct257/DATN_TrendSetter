package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.DTO.ShippingCostResponse;
import com.example.datn_trendsetter.Entity.*;
import com.example.datn_trendsetter.Service.GhnApiService;
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
                              Model model) {
        shopService.getHoaDonAndProducts(hoaDonId, model);
        return "Admin/dashboard"; // Return to the page where the form is rendered
    }


    @GetMapping("admin/add-product-modal")
    public String addproductmodal(@RequestParam(value = "hoaDonId", required = false) Integer hoaDonId,
                              Model model) {
        shopService.getProducts(hoaDonId, model);
        return "Admin/add-product-modal"; // Return to the page where the form is rendered
    }

    @PostMapping("create-hoa-don")
    public String createHoaDon(HoaDon hoaDon,RedirectAttributes redirectAttributes) {
        try {
            shopService.createHoaDon(hoaDon);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo hóa đơn thành công");
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/sell-counter";

    }

    @PostMapping("delete/{id}")
    public String deleteHoaDon(@PathVariable("id") Integer hoaDonId, RedirectAttributes redirectAttributes) {
        try {
            // Gọi service để xóa hóa đơn
            shopService.deleteHoaDon(hoaDonId);

            // Thêm thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "Hóa đơn đã được xóa thành công!");

        } catch (Exception e) {
            // Thêm thông báo lỗi nếu có ngoại lệ
            redirectAttributes.addFlashAttribute("errorMessage", "Xóa hóa đơn thất bại: " + e.getMessage());
        }

        // Chuyển hướng về trang danh sách hóa đơn
        return "redirect:/admin/sell-counter";
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


    @PostMapping("delete-product-order")
    public String deleteProductOrder(@RequestParam("hoaDonChiTietId") Integer hoaDonChiTietId,
                                     @RequestParam("hoaDonId") Integer hoaDonId,
                                     RedirectAttributes redirectAttributes) {
        return shopService.handleProductOrder("delete", null, hoaDonId, null, hoaDonChiTietId, redirectAttributes);
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



    @PostMapping("/update-shipping")
    public String updateShipping(@RequestParam("hoaDonId") Integer hoaDonId,
                                 @RequestParam("soNha") Integer soNha,
                                 @RequestParam("tenDuong") String tenDuong,
                                 @RequestParam("phuong") String phuong,
                                 @RequestParam("huyen") String huyen,
                                 @RequestParam("thanhPho") String thanhPho,
                                 RedirectAttributes redirectAttributes) {
        return shopService.updateShippingAddress(hoaDonId, soNha, tenDuong, phuong, huyen, thanhPho, redirectAttributes);
    }



    @PostMapping("/add-new-customer")
    public String addNewCustomer(@RequestParam("hoaDonId") Integer hoaDonId,
                                 @RequestParam("nguoiNhan") String nguoiNhan,
                                 @RequestParam("soDienThoai") String soDienThoai,
                                 RedirectAttributes redirectAttributes) {

        return shopService.addNewCustomer(hoaDonId,nguoiNhan, soDienThoai, redirectAttributes);
    }


}

