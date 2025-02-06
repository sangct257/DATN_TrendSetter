package com.example.datn_trendsetter.Controller.Admin;

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

//    @Autowired
//    private ShopService shopService;
//
//    @Autowired
//    private HoaDonRepository hoaDonRepository;
//
//    @Autowired
//    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;
//
//    @GetMapping("/sell-counter")
//    public String sellCounter(@RequestParam(value = "hoaDonId", required = false) Integer hoaDonId,
//                              Model model,
//                              RedirectAttributes redirectAttributes) {
//        shopService.getHoaDonAndProducts(hoaDonId, model, redirectAttributes);
//        return "dashboard"; // Return to the page where the form is rendered
//    }
//
//    @PostMapping("/create-hoa-don")
//    public String createHoaDon(HoaDon hoaDon, RedirectAttributes redirectAttributes) {
//        return shopService.createHoaDon(hoaDon, redirectAttributes);
//    }
//
//    @PostMapping("/add-product-order")
//    public String addProductOrder(@RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
//                                  @RequestParam("idHoaDon") Integer idHoaDon,
//                                  @RequestParam("soLuong") Integer soLuong,
//                                  RedirectAttributes redirectAttributes) {
//        return shopService.handleProductOrder("add", idSanPhamChiTiet, idHoaDon, soLuong, null, redirectAttributes);
//    }
//
//    @PostMapping("/update-product-order")
//    public String updateQuantityOrder(@RequestParam("idHoaDonChiTiet") Integer idHoaDonChiTiet,
//                                      @RequestParam("soLuong") Integer soLuong,
//                                      @RequestParam("idHoaDon") Integer idHoaDon,
//                                      RedirectAttributes redirectAttributes) {
//        return shopService.handleProductOrder("update", null, idHoaDon, soLuong, idHoaDonChiTiet, redirectAttributes);
//    }
//
//    @PostMapping("/delete-product-order")
//    public String deleteProductOrder(@RequestParam("idHoaDonChiTiet") Integer idHoaDonChiTiet,
//                                     @RequestParam("idHoaDon") Integer idHoaDon,
//                                     RedirectAttributes redirectAttributes) {
//        return shopService.handleProductOrder("delete", null, idHoaDon, null, idHoaDonChiTiet, redirectAttributes);
//    }
//
//    @PostMapping("/add-customer")
//    public String addCustomerToInvoice(@RequestParam("hoaDonId") Integer hoaDonId,
//                                       @RequestParam("khachHangId") Integer khachHangId,
//                                       RedirectAttributes redirectAttributes) {
//        return shopService.addCustomerToInvoice(hoaDonId, khachHangId, redirectAttributes);
//    }
//
//
//    @PostMapping("/add-payment-method")
//    @ResponseBody
//    public ResponseEntity<Map<String, String>> addPaymentMethod(@RequestParam("hoaDonId") Integer hoaDonId,
//                                                                @RequestParam("phuongThucThanhToan") Integer phuongThucThanhToanId) {
//
//        // Lấy hóa đơn từ cơ sở dữ liệu
//        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
//        if (hoaDon == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Hóa đơn không tồn tại"));
//        }
//
//        // Lấy phương thức thanh toán từ cơ sở dữ liệu
//        PhuongThucThanhToan phuongThucThanhToan = phuongThucThanhToanRepository.findById(phuongThucThanhToanId).orElse(null);
//        if (phuongThucThanhToan == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Phương thức thanh toán không tồn tại"));
//        }
//
//        // Cập nhật phương thức thanh toán cho hóa đơn
//        hoaDon.setPhuongThucThanhToan(phuongThucThanhToan);
//        hoaDonRepository.save(hoaDon);
//
//        // Trả về thông báo thành công và tên phương thức thanh toán đã được cập nhật
//        return ResponseEntity.ok(Map.of("success", "Phương thức thanh toán đã được cập nhật thành công!",
//                "updatedPaymentMethod", phuongThucThanhToan.getTenPhuongThuc()));
//    }
//
//
//    @PostMapping("/apply-phieu-giam-gia")
//    public String applyPhieuGiamGia(@RequestParam("hoaDonId") Integer hoaDonId,
//                                 @RequestParam("tenChuongTrinh") String tenChuongTrinh,
//                                 RedirectAttributes redirectAttributes) {
//        return shopService.applyPhieuGiamGia(hoaDonId, tenChuongTrinh, redirectAttributes);
//    }
//
//    @PostMapping("/confirm-payment")
//    public String confirmPayment(@RequestParam("hoaDonId") Integer hoaDonId,
//                                 RedirectAttributes redirectAttributes) {
//        return shopService.confirmPayment(hoaDonId, redirectAttributes);
//    }
//
////    @PostMapping("/update-shipping")
////    public String updateShipping(@ModelAttribute ShippingUpdateRequest request,
////                                 Integer hoaDonId,
////                                 RedirectAttributes redirectAttributes) {
////        return shopService.updateShippingAddress(request, hoaDonId, redirectAttributes);
////    }
//
//    @PostMapping("/remove-phieu-giam-gia")
//    public String removeDiscount(@RequestParam("hoaDonId") Integer hoaDonId) {
//        return shopService.removePhieuGiamGia(hoaDonId);
//    }
//
//    @PostMapping("/update-phi-ship")
//    public String updatePhiShip(@RequestParam("hoaDonId") Integer hoaDonId,Double phiShip,RedirectAttributes redirectAttributes) {
//        return shopService.updatePhiShip(hoaDonId,phiShip,redirectAttributes);
//    }


}

