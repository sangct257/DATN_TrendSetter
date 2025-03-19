package com.example.datn_trendsetter.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String soDienThoai;
    private Date ngaySinh;
    private String role;
}

