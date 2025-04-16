package com.example.datn_trendsetter.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true) // Đảm bảo mỗi user chỉ có 1 token
    private KhachHang user;

    // Thêm constructor để tự động set expiryDate
    public ResetToken() {
        this.expiryDate = LocalDateTime.now().plusMinutes(15); // 15 phút hết hạn
    }
}