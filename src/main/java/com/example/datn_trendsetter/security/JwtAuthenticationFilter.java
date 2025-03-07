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

        System.out.println("🔍 Kiểm tra JWT Filter...");

        // ✅ In toàn bộ headers để kiểm tra
        request.getHeaderNames().asIterator().forEachRemaining(headerName ->
                System.out.println("📌 Header: " + headerName + " = " + request.getHeader(headerName))
        );

        String authHeader = request.getHeader("Authorization");

        // Kiểm tra header có chứa token hay không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ Không có token hợp lệ trong request.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);  // Lấy token từ header
        System.out.println("✅ Token nhận được: " + token);

        // Kiểm tra tính hợp lệ của token
        if (!jwtUtil.validateToken(token)) {
            System.out.println("❌ Token không hợp lệ!");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
            return;
        }

        // Lấy thông tin người dùng từ token
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        System.out.println("✅ Username từ token: " + username);
        System.out.println("✅ Role từ token: " + role);

        // Kiểm tra người dùng có tồn tại trong hệ thống không
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Tạo đối tượng Authentication và set vào SecurityContext
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Đảm bảo Authentication đã được set vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("✅ Đã set Authentication vào SecurityContext");

        filterChain.doFilter(request, response);  // Tiếp tục chuỗi lọc
    }
}
