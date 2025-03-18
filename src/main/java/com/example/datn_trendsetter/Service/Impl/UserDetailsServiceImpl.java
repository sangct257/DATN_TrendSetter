package com.example.datn_trendsetter.Service.Impl;

import com.example.datn_trendsetter.Entity.NhanVien;
import com.example.datn_trendsetter.Repository.NhanVienRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final NhanVienRepository nhanVienRepository;

    public UserDetailsServiceImpl(NhanVienRepository nhanVienRepository) {
        this.nhanVienRepository = nhanVienRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy nhân viên với email: " + email));

        String role = nhanVien.getVaiTro().name();
        System.out.println("User: " + email + " - Role: " + role); // In ra console

        return new org.springframework.security.core.userdetails.User(
                nhanVien.getEmail(),
                nhanVien.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }


}