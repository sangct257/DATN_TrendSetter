package com.example.datn_trendsetter.API;

import aj.org.objectweb.asm.TypeReference;
import com.example.datn_trendsetter.DTO.ProductOrderRequest;
import com.example.datn_trendsetter.DTO.SanPhamChiTietDTO;
import com.example.datn_trendsetter.Entity.DiaChi;
import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Repository.KhachHangRepository;
import com.example.datn_trendsetter.Repository.SanPhamChiTietRepository;
import com.example.datn_trendsetter.Service.ShopService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ShopApiController {
    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private ShopService shopService;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createHoaDon(HttpSession session) {
        try {
            // Lấy trực tiếp đối tượng NhanVien từ session
            NhanVien nhanVien = (NhanVien) session.getAttribute("userNhanVien");

            if (nhanVien == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Vui lòng đăng nhập"));
            }

            // Tạo hóa đơn mới
            HoaDon hoaDon = new HoaDon();
            HoaDon createdHoaDon = shopService.createHoaDon(hoaDon, nhanVien.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", createdHoaDon.getId(),
                    "maHoaDon", createdHoaDon.getMaHoaDon(),
                    "message", "Tạo hóa đơn thành công"
            ));

        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Định dạng session không hợp lệ"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server: " + e.getMessage()));
        }
    }


    @PostMapping("delete/{id}")
    public ResponseEntity<Map<String, String>> deleteHoaDon(@PathVariable("id") Integer hoaDonId, HttpSession session) {
        Map<String, String> response = new HashMap<>();
        try {
            // Lấy trực tiếp đối tượng NhanVien từ session
            NhanVien nhanVien = (NhanVien) session.getAttribute("userNhanVien");

            if (nhanVien == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Vui lòng đăng nhập"));
            }

            shopService.deleteHoaDon(hoaDonId);
            response.put("message", "🗑️ Hóa đơn đã được xóa!");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", "🚨 " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("error", "❌ Lỗi khi xóa hóa đơn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/add-customer")
    public ResponseEntity<?> addCustomerToInvoice(@RequestParam("hoaDonId") Integer hoaDonId,
                                                  @RequestParam("khachHangId") Integer khachHangId,
                                                  HttpSession session) {
        try {
            // Lấy trực tiếp đối tượng NhanVien từ session
            NhanVien nhanVien = (NhanVien) session.getAttribute("userNhanVien");

            if (nhanVien == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Vui lòng đăng nhập"));
            }

            // Tìm khách hàng từ khachHangId
            KhachHang khachHang = khachHangRepository.findById(khachHangId)
                    .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại!"));

            // Gọi service để thêm khách hàng vào hóa đơn
            String message = shopService.addCustomerToInvoice(hoaDonId, khachHang);
            return ResponseEntity.ok(Collections.singletonMap("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }


    @PostMapping("/delete-customer")
    public ResponseEntity<Map<String, String>> deleteCustomerToInvoice(@RequestParam("hoaDonId") Integer hoaDonId) {
        Map<String, String> response = shopService.deleteCustomerToInvoice(hoaDonId);
        return response.containsKey("error") ?
                ResponseEntity.badRequest().body(response) :
                ResponseEntity.ok(response);
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

    @PostMapping("/apply-phieu-giam-gia")
    public ResponseEntity<Map<String, Object>> applyPhieuGiamGia(
            @RequestParam("hoaDonId") Integer hoaDonId,
            @RequestParam(value = "tenPhieuGiamGia", required = false) String tenPhieuGiamGia) {

        Map<String, Object> response = new HashMap<>();
        try {
            if (hoaDonId == null) {
                throw new IllegalArgumentException("ID hóa đơn không hợp lệ.");
            }

            // Gửi xuống service, chấp nhận cả trường hợp tenPhieuGiamGia rỗng (bỏ giảm giá)
            String message = shopService.applyPhieuGiamGia(hoaDonId, (tenPhieuGiamGia != null) ? tenPhieuGiamGia.trim() : "");

            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @PutMapping("/update-shipping")
    public ResponseEntity<Map<String, String>> updateShipping(@RequestParam Integer hoaDonId,
                                                              @RequestParam String nguoiNhan,
                                                              @RequestParam String soDienThoai,
                                                              @RequestParam String diaChiCuThe,
                                                              @RequestParam String phuong,
                                                              @RequestParam String huyen,
                                                              @RequestParam String thanhPho,
                                                              @RequestParam String ghiChu) {
        Map<String, String> response = new HashMap<>();
        try {
            String message = shopService.updateShippingAddress(hoaDonId, nguoiNhan, soDienThoai, diaChiCuThe, phuong, huyen, thanhPho, ghiChu);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage()); // Trả về thông báo lỗi
            return ResponseEntity.badRequest().body(response); // Trả về mã lỗi 400 với thông điệp
        }
    }



    @PostMapping("/add-new-customer")
    public ResponseEntity<Map<String, Object>> addNewCustomer(@RequestParam("hoaDonId") Integer hoaDonId,
                                                              @RequestParam("nguoiNhan") String nguoiNhan,
                                                              @RequestParam("soDienThoai") String soDienThoai) {
        Map<String, Object> response = shopService.addNewCustomer(hoaDonId, nguoiNhan, soDienThoai);

        // Nếu trạng thái không phải success thì trả về Bad Request
        if (!"success".equals(response.get("status"))) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-product-order")
    public ResponseEntity<Map<String,String>> addProductOrder(@RequestBody ProductOrderRequest orderRequest) {
        Integer sanPhamChiTietId = orderRequest.getSanPhamChiTietId();
        Integer hoaDonId = orderRequest.getHoaDonId();
        Integer soLuong = orderRequest.getSoLuong();

        return shopService.handleProductOrder("add", sanPhamChiTietId, hoaDonId, soLuong, null);
    }


    @PostMapping("update-product-order")
    public ResponseEntity<Map<String,String>> updateQuantityOrder(@RequestBody ProductOrderRequest orderRequest) {

        return shopService.handleProductOrder("update", null, orderRequest.getHoaDonId(), orderRequest.getSoLuong(), orderRequest.getHoaDonChiTietId());
    }


    @PostMapping("delete-product-order")
    public ResponseEntity<Map<String,String>> deleteProductOrder(@RequestBody ProductOrderRequest orderRequest) {
        return shopService.handleProductOrder("delete", null, orderRequest.getHoaDonId(), null,orderRequest.getHoaDonChiTietId());
    }
    @PutMapping("/cap-nhat-loai-giao-dich/{id}")
    public ResponseEntity<Map<String, Object>> capNhatLoaiGiaoDich(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy trực tiếp đối tượng NhanVien từ session
            NhanVien nhanVien = (NhanVien) session.getAttribute("userNhanVien");

            if (nhanVien == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Vui lòng đăng nhập"));
            }

            Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(id);
            if (hoaDonOpt.isEmpty()) {
                response.put("errorMessage", "Hóa đơn không tồn tại!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            HoaDon hoaDon = hoaDonOpt.get();

//            // Kiểm tra xem nhân viên hiện tại có phải là người tạo hóa đơn không
//            NhanVien currentNhanVien = (NhanVien) user;
//            if (!currentNhanVien.equals(hoaDon.getNhanVien())) {
//                response.put("errorMessage", "Bạn không có quyền chỉnh sửa hóa đơn này");
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//            }

            if ("Tại Quầy".equals(hoaDon.getLoaiHoaDon())) {
                hoaDon.setPhiShip(null);
            } else if ("Giao Hàng".equals(hoaDon.getLoaiHoaDon())) {
                if (hoaDon.getNguoiNhan() != null && hoaDon.getSoDienThoai() != null) {
                    hoaDon.setPhiShip(30000F);
                } else {
                    hoaDon.setPhiShip(0F);
                }
            } else {
                response.put("errorMessage", "Loại hóa đơn không hợp lệ!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            hoaDon.setLoaiGiaoDich("Trả Sau".equals(hoaDon.getLoaiGiaoDich()) ? "Trả Trước" : "Trả Sau");
            hoaDonRepository.save(hoaDon);

            response.put("successMessage", "Cập nhật loại giao dịch thành công!");
            response.put("loaiGiaoDich", hoaDon.getLoaiGiaoDich());
            response.put("phiShip", hoaDon.getPhiShip());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("errorMessage", "Lỗi cập nhật loại giao dịch!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
