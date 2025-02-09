package com.example.datn_trendsetter.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "danh_gia")
public class DanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "so_sao")
    private Integer soSao;

    @Column(name = "nhan_xet",columnDefinition = "NVARCHAR(255)")
    private String nhanXet;

    @Column(name = "ngay_danh_gia")
    private LocalDateTime ngayDanhGia;
}
