package com.example.datn_trendsetter.API;

import com.example.datn_trendsetter.DTO.DiaChiDTO;
import com.example.datn_trendsetter.Entity.DiaChi;
import com.example.datn_trendsetter.Repository.DiaChiRepository;
import com.example.datn_trendsetter.Service.DiaChiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/dia-chi")
public class DiaChiApiController {
    @Autowired
    private DiaChiRepository diaChiRepository;

    @Autowired
    private DiaChiService diaChiService;

    @PostMapping("/add")
    public ResponseEntity<?> addDiaChi(@RequestBody DiaChi diaChi, @RequestParam Integer idKhachHang) {
        try {
            if (idKhachHang == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Khách hàng không được để trống.");
            }

            DiaChi savedDiaChi = diaChiService.addDiaChi(diaChi, idKhachHang);
            return ResponseEntity.ok(savedDiaChi);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi thêm địa chỉ: " + e.getMessage());
        }
    }


    @GetMapping("/{idDiaChi}")
    public ResponseEntity<?> getDiaChiById(@PathVariable Integer idDiaChi) {
        Optional<DiaChi> optionalDiaChi = diaChiRepository.findById(idDiaChi);
        if (optionalDiaChi.isPresent()) {
            return ResponseEntity.ok(optionalDiaChi.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy địa chỉ với ID: " + idDiaChi);
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiaChi(@PathVariable Integer id, @RequestBody DiaChi updatedDiaChi) {
        System.out.println("ID nhận được: " + id); // Debug ID nhận từ request
        Optional<DiaChi> optionalDiaChi = diaChiRepository.findById(id);

        if (!optionalDiaChi.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy địa chỉ với ID: " + id);
        }

        DiaChi diaChi = optionalDiaChi.get();
        diaChi.setDiaChiCuThe(updatedDiaChi.getDiaChiCuThe());
        diaChi.setPhuong(updatedDiaChi.getPhuong());
        diaChi.setHuyen(updatedDiaChi.getHuyen());
        diaChi.setThanhPho(updatedDiaChi.getThanhPho());
        diaChi.setTrangThai(diaChi.getTrangThai());

        diaChiRepository.save(diaChi);
        return ResponseEntity.ok("Cập nhật địa chỉ thành công!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDiaChi(@PathVariable Integer id) {
        try {
            diaChiService.deleteDiaChi(id);
            return ResponseEntity.ok("Xóa địa chỉ thành công!");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy địa chỉ với ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa địa chỉ: " + e.getMessage());
        }
    }

}
