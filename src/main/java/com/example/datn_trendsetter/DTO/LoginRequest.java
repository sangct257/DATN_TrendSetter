package com.example.datn_trendsetter.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String email;
    private String password;
    private String loaiTaiKhoan;

}
