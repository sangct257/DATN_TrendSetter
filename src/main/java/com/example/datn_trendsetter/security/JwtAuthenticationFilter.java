package com.example.datn_trendsetter.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ Không tìm thấy Authorization header hợp lệ.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            System.out.println("❌ Token không hợp lệ hoặc đã hết hạn.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
            return;
        }

        String username = jwtUtil.getUsernameFromToken(token);
        List<String> roles = jwtUtil.getRolesFromToken(token);
        String requestURI = request.getRequestURI();

        System.out.println("✅ Token nhận được: " + token);
        System.out.println("✅ Username từ token: " + username);
        System.out.println("✅ Roles từ token: " + roles);
        System.out.println("🔹 Yêu cầu đến: " + requestURI);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            if (!isAuthorized(roles, requestURI)) {
                System.out.println("❌ Người dùng không có quyền truy cập: " + requestURI);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập vào đường dẫn này.");
                return;
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Kiểm tra trước khi set authentication
            System.out.println("🔹 Trước khi set authentication: " + SecurityContextHolder.getContext().getAuthentication());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // Kiểm tra sau khi set authentication
            System.out.println("✅ Sau khi set authentication: " + SecurityContextHolder.getContext().getAuthentication());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthorized(List<String> roles, String requestURI) {
        if (roles.contains("ROLE_ADMIN")) {
            return true; // Admin truy cập mọi đường dẫn
        }
        if (roles.contains("ROLE_KHACHHANG") && requestURI.startsWith("/trendsetter")) {
            return true; // Khách hàng chỉ được vào /trendsetter
        }
        if (roles.contains("ROLE_NHANVIEN")) {
            return !(requestURI.startsWith("/admin/thong-ke") || requestURI.startsWith("/admin/quan-ly-tai-khoan"));
        }
        return false;
    }
}
