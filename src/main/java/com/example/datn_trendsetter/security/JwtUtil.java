package com.example.datn_trendsetter.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "trendsettertrendsettertrendsettertrendsetter"; // Ít nhất 32 ký tự
    private static final long EXPIRATION_TIME = 86400000; // 1 ngày

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String username, List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            roles = List.of("USER");
        }

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles) // Lưu danh sách roles đúng định dạng
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public List<String> getRolesFromToken(String token) {
        Object rolesObject = extractClaim(token, claims -> claims.get("roles"));

        if (rolesObject instanceof String) {
            return Arrays.asList(((String) rolesObject).split(",")); // Chuyển từ String sang List<String>
        } else if (rolesObject instanceof List<?>) {
            return ((List<?>) rolesObject).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
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