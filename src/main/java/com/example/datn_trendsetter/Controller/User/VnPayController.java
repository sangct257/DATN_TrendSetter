package com.example.datn_trendsetter.Controller.User;

import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Entity.PhieuGiamGia;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Repository.PhieuGiamGiaRepository;
import com.example.datn_trendsetter.Service.VnPayService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequestMapping("/api/payment")
public class VnPayController {
    @Autowired
    private VnPayService vnPayService;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        System.out.println("🔍 Dữ liệu nhận được từ frontend: " + requestData);

        try {
            // ✅ Lấy orderId và loại bỏ ký tự chữ (nếu có)
            String rawOrderId = String.valueOf(requestData.get("orderId"));
            String orderId = rawOrderId.replaceAll("\\D", "");

            if (orderId.isEmpty()) {
                throw new IllegalArgumentException("Mã hóa đơn không hợp lệ (phải chứa ít nhất một số).");
            }

            // ✅ Đảm bảo amount là số nguyên
            Integer amount = ((Number) requestData.get("amount")).intValue();

            System.out.println("📌 orderId gốc: " + rawOrderId);
            System.out.println("📌 orderId (sau khi loại bỏ ký tự): " + orderId);
            System.out.println("📌 amount (gốc): " + amount);

            // ✅ Chuyển IP của client
            String ipAddr = request.getRemoteAddr();
            String paymentUrl = vnPayService.createPaymentUrl(orderId, amount, ipAddr);

            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi xử lý dữ liệu: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> vnp_Params, Model model) {
        boolean success = "00".equals(vnp_Params.get("vnp_ResponseCode"));

        // Thêm "HD" vào trước mã đơn hàng
        String orderId = "HD" + vnp_Params.get("vnp_TxnRef");

        // Chuyển đổi thời gian thanh toán
        String rawDate = vnp_Params.get("vnp_PayDate");
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDate = LocalDateTime.parse(rawDate, inputFormatter).format(outputFormatter);

        if (success) {
            HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(orderId);
            if (hoaDon != null) {
                hoaDon.setTrangThai("Chờ Xác Nhận");

                // ✅ Xử lý trừ phiếu giảm giá
                PhieuGiamGia phieuGiamGia = hoaDon.getPhieuGiamGia();
                if (phieuGiamGia != null) {
                    if (phieuGiamGia.getSoLuotSuDung() > 0) {
                        phieuGiamGia.setSoLuotSuDung(phieuGiamGia.getSoLuotSuDung() - 1);
                        phieuGiamGia.setTrangThai(phieuGiamGia.getSoLuotSuDung() > 0 ? "Đang Hoạt Động" : "Ngừng Hoạt Động");
                        phieuGiamGiaRepository.save(phieuGiamGia);
                    }
                }

                hoaDonRepository.save(hoaDon);
            }
        }

        model.addAttribute("orderId", orderId);
        model.addAttribute("totalPrice", Integer.parseInt(vnp_Params.get("vnp_Amount")) / 100);
        model.addAttribute("paymentTime", formattedDate);
        model.addAttribute("transactionId", vnp_Params.get("vnp_TransactionNo"));

        return success ? "User/orderSuccess" : "User/orderFail";
    }


}