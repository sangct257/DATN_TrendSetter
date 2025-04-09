package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.HoaDonDTO;
import com.example.datn_trendsetter.DTO.HoaDonResponseDto;
import com.example.datn_trendsetter.Entity.DiaChi;
import com.example.datn_trendsetter.Entity.HoaDon;
import com.example.datn_trendsetter.Entity.KhachHang;
import com.example.datn_trendsetter.Repository.HoaDonRepository;
import com.example.datn_trendsetter.Service.ShopService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hoa-don")
public class HoaDonApiController {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private ShopService shopService;

//    @PostMapping("/create-hoa-don")
//    public String createHoaDon(
//            @ModelAttribute HoaDon hoaDon,
//            RedirectAttributes redirectAttributes,
//            HttpSession session
//    ) {
//        try {
//            // Lấy ID nhân viên từ session
//            Map<String, Object> userData = (Map<String, Object>) session.getAttribute("user");
//            Integer nhanVienId = (Integer) ((Map<String, Object>) userData.get("user")).get("id");
//
//            // Gọi service
//            HoaDon createdHoaDon = shopService.createHoaDon(hoaDon, nhanVienId);
//
//            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo hóa đơn thành công");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//        }
//
//        return "redirect:/admin/sell-counter";
//    }


    @PutMapping("/toggle-delivery/{id}")
    public ResponseEntity<Map<String, Object>> toggleDelivery(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(id);
            if (!optionalHoaDon.isPresent()) {
                response.put("errorMessage", "Không tìm thấy hóa đơn!");
                return ResponseEntity.badRequest().body(response);
            }

            HoaDon hoaDon = optionalHoaDon.get();

            // Kiểm tra trạng thái và cập nhật
            if ("Tại Quầy".equals(hoaDon.getLoaiHoaDon())) {
                hoaDon.setLoaiGiaoDich("Trả Sau");
                hoaDon.setLoaiHoaDon("Giao Hàng");
            } else if ("Giao Hàng".equals(hoaDon.getLoaiHoaDon())) {
                hoaDon.setLoaiGiaoDich("Đã Thanh Toán");
                hoaDon.setLoaiHoaDon("Tại Quầy");
            }

            hoaDonRepository.save(hoaDon);
            response.put("successMessage", "Cập nhật trạng thái hóa đơn thành công!");
            response.put("loaiHoaDon", hoaDon.getLoaiHoaDon()); // Gửi trạng thái mới về frontend

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("errorMessage", "Lỗi cập nhật hóa đơn!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getHoaDonByTrangThai(@RequestParam(required = false) String trangThai) {
        List<HoaDon> hoaDonList;
        if (trangThai != null && !trangThai.isEmpty()) {
            hoaDonList = hoaDonRepository.findByTrangThai(trangThai, Sort.by(Sort.Direction.DESC, "id"));
        } else {
            hoaDonList = hoaDonRepository.findByTrangThaiNot("Đang Xử Lý", Sort.by(Sort.Direction.DESC, "id"));
        }

        // Chuyển đổi danh sách hóa đơn sang JSON hợp lệ
        List<Map<String, Object>> jsonResponse = hoaDonList.stream().map(hoaDon -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", hoaDon.getId());
            map.put("maHoaDon", hoaDon.getMaHoaDon());
            map.put("nguoiNhan", hoaDon.getNguoiNhan() != null ? hoaDon.getNguoiNhan() : "Khách lẻ");
            map.put("nguoiTao", hoaDon.getNguoiTao() != null ? hoaDon.getNguoiTao() : "Không có dữ liệu");
            map.put("loaiHoaDon", hoaDon.getLoaiHoaDon() != null ? hoaDon.getLoaiHoaDon() : "Không có dữ liệu");
            map.put("ngayTao", hoaDon.getNgayTao() != null ? hoaDon.getNgayTao().toString() : "Không rõ ngày");
            map.put("phieuGiamGia", hoaDon.getPhieuGiamGia() != null ? Map.of("giaTriGiam", hoaDon.getPhieuGiamGia().getGiaTriGiam()) : null);
            map.put("tongTien", hoaDon.getTongTien() != null ? hoaDon.getTongTien() : 0);
            return map;
        }).toList();

        return ResponseEntity.ok(jsonResponse);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countDotGiamGiaByTrangThai() {
        long dangXuLy = hoaDonRepository.countByTrangThai("Đang Xử Lý");
        long choXacNhan = hoaDonRepository.countByTrangThai("Chờ Xác Nhận");
        long daXacNhan = hoaDonRepository.countByTrangThai("Đã Xác Nhận");
        long choVanChuyen = hoaDonRepository.countByTrangThai("Chờ Vận Chuyển");
        long dangGiaoHang = hoaDonRepository.countByTrangThai("Đang Giao Hàng");
        long daThanhToan = hoaDonRepository.countByTrangThai("Đã Thanh Toán");
        long daHoanThanh = hoaDonRepository.countByTrangThai("Đã Hoàn Thành");
        long hoanTra = hoaDonRepository.countByTrangThai("Hoàn Trả");
        long daHuy = hoaDonRepository.countByTrangThai("Đã Hủy");
        // Tổng tất cả trừ "Đang Xử Lý"
        long tong = hoaDonRepository.countByTrangThaiNot("Đang Xử Lý");

        Map<String, Long> coutMap = Map.of(
                "Đang Xử Lý",dangXuLy,
                "Chờ Xác Nhận",choXacNhan,
                "Đã Xác Nhận",daXacNhan,
                "Chờ Vận Chuyển",choVanChuyen,
                "Đang Giao Hàng",dangGiaoHang,
                "Đã Thanh Toán",daThanhToan,
                "Đã Hoàn Thành",daHoanThanh,
                "Hoàn Trả",hoanTra,
                "Đã Hủy",daHuy,
                "Tất Cả",tong
        );

        return ResponseEntity.ok().body(coutMap);
    }

    @GetMapping("/list-all")
    public ResponseEntity<?> getHoaDonByKhachHang(HttpSession session) {
        // Kiểm tra đăng nhập
        KhachHang khachHang = (KhachHang) session.getAttribute("userKhachHang");
        if (khachHang == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Chưa đăng nhập"
            ));
        }

        System.out.println("KhachHang ID từ session: " + khachHang.getId());

        // Gọi phương thức tìm kiếm với JOIN FETCH
        List<HoaDon> danhSachHoaDon = hoaDonRepository.findByKhachHangIdWithKhachHang(khachHang.getId());

        if (danhSachHoaDon.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of(
                    "success", false,
                    "message", "Không có hóa đơn nào"
            ));
        }

        // Chuyển đổi danh sách HoaDon thành danh sách DTO
        List<HoaDonDTO> hoaDonDTOList = danhSachHoaDon.stream().map(hoaDon -> new HoaDonDTO(
                hoaDon.getId(),
                hoaDon.getMaHoaDon(),
                hoaDon.getKhachHang().getId(),
                hoaDon.getNguoiTao(),
                hoaDon.getLoaiHoaDon(),
                hoaDon.getNgayTao(),
                hoaDon.getTongTien(),
                hoaDon.getPhiShip()
        )).collect(Collectors.toList());

        // Log danh sách hóa đơn và ID của mỗi hóa đơn
        for (HoaDonDTO hoaDonDTO : hoaDonDTOList) {
            System.out.println("Hoa Don ID: " + hoaDonDTO.getMaHoaDon() + ", Khach Hang ID: " + khachHang.getId());
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lấy danh sách hóa đơn thành công",
                "data", hoaDonDTOList
        ));
    }

}
