package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.ProductOrderRequest;
import com.example.datn_trendsetter.DTO.SanPhamChiTietDTO;
import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Repository.SanPhamChiTietRepository;
import com.example.datn_trendsetter.Service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class ShopApiController {
    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private ShopService shopService;

    @GetMapping("/suggest-products")
    @ResponseBody
    public ResponseEntity<List<SanPhamChiTietDTO>> suggestProducts(@RequestParam("search") String search) {
        List<SanPhamChiTietDTO> suggestions = sanPhamChiTietRepository.suggestSanPhamAndMauSacAndKichThuoc(search);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createHoaDon() {
        try {
            HoaDon hoaDon = new HoaDon();
            hoaDon.setKhachHang(null);  // Hoặc gán khách hàng mặc định nếu cần
            hoaDon.setNhanVien(null);   // Hoặc gán nhân viên mặc định nếu cần

            HoaDon createdHoaDon = shopService.createHoaDon(hoaDon);

            if (createdHoaDon == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Không thể tạo hóa đơn, vui lòng thử lại.");
            }

            return ResponseEntity.ok(createdHoaDon);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi tạo hóa đơn: " + e.getMessage());
        }
    }


    @PostMapping("delete/{id}")
    public ResponseEntity<Map<String, String>> deleteHoaDon(@PathVariable("id") Integer hoaDonId) {
        Map<String, String> response = new HashMap<>();
        try {
            // Gọi service để xóa hóa đơn
            shopService.deleteHoaDon(hoaDonId);

            // Trả về JSON thông báo thành công
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
                                                  @RequestParam("khachHangId") Integer khachHangId) {
        try {
            String message = shopService.addCustomerToInvoice(hoaDonId, khachHangId);
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
            @RequestParam("tenPhieuGiamGia") String tenPhieuGiamGia) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (hoaDonId == null || tenPhieuGiamGia == null || tenPhieuGiamGia.isEmpty()) {
                throw new IllegalArgumentException("Dữ liệu không hợp lệ");
            }

            String message = shopService.applyPhieuGiamGia(hoaDonId, tenPhieuGiamGia);

            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
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
                                                              @RequestParam Integer soNha,
                                                              @RequestParam String tenDuong,
                                                              @RequestParam String phuong,
                                                              @RequestParam String huyen,
                                                              @RequestParam String thanhPho,
                                                              @RequestParam String ghiChu) {
        Map<String, String> response = new HashMap<>();
        try {
            String message = shopService.updateShippingAddress(hoaDonId, nguoiNhan, soDienThoai, soNha, tenDuong, phuong, huyen, thanhPho, ghiChu);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage()); // Trả về thông báo lỗi
            return ResponseEntity.badRequest().body(response); // Trả về mã lỗi 400 với thông điệp
        }
    }



    @PostMapping("/add-new-customer")
    public ResponseEntity<Map<String, String>> addNewCustomer(@RequestParam("hoaDonId") Integer hoaDonId,
                                                              @RequestParam("nguoiNhan") String nguoiNhan,
                                                              @RequestParam("soDienThoai") String soDienThoai) {
        Map<String, String> response = shopService.addNewCustomer(hoaDonId, nguoiNhan, soDienThoai);

        if (response.containsKey("error")) {
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
    public ResponseEntity<Map<String, Object>> capNhatLoaiGiaoDich(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(id);
            if (hoaDonOpt.isEmpty()) {
                response.put("errorMessage", "Hóa đơn không tồn tại!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            HoaDon hoaDon = hoaDonOpt.get();

            if ("Tại Quầy".equals(hoaDon.getLoaiHoaDon())) {
                hoaDon.setPhiShip(null); // Tại quầy thì phí ship null
            } else if ("Giao Hàng".equals(hoaDon.getLoaiHoaDon())) {
                // Nếu là giao hàng, tự động tính phí ship
                float phiShip = shopService.tinhPhiShip(hoaDon.getThanhPho(), hoaDon.getHuyen());
                hoaDon.setPhiShip(phiShip);
            } else {
                response.put("errorMessage", "Loại hóa đơn không hợp lệ!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Chuyển đổi trạng thái loại giao dịch
            hoaDon.setLoaiGiaoDich("Trả Sau".equals(hoaDon.getLoaiGiaoDich()) ? "Đã Thanh Toán" : "Trả Sau");

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
