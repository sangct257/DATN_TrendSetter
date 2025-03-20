package com.example.datn_trendsetter.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "trendsettertrendsettertrendsettertrendsetter";
    private static final long EXPIRATION_TIME = 86400000; // 1 ngày

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String username, String email, String hoTen, String hinhAnh, String vaiTro) {
        return Jwts.builder()
                .setSubject(username)
                .claim("email", email)
                .claim("ho_ten", hoTen)
                .claim("hinh_anh", hinhAnh)
                .claim("vai_tro", vaiTro != null ? vaiTro : "KHACHHANG") // Mặc định vai trò khách hàng
                .claim("login_time", System.currentTimeMillis())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String getVaiTroFromToken(String token) {
        return extractClaim(token, claims -> claims.get("vai_tro", String.class));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            System.out.println("❌ JWT Error: " + e.getMessage());
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}
