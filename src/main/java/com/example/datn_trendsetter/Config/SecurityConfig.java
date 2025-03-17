package com.example.datn_trendsetter.Config;

import com.example.datn_trendsetter.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // ❌ Tắt CSRF (nếu dùng API REST)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        // ✅ Cho phép truy cập không cần token
//                        .requestMatchers("/auth/home", "/auth/login", "/auth/register", "/api", "/trang-chu",
//                                "/css/**", "/js/**", "/images/**", "/static/**").permitAll()
//                        // ✅ Phân quyền
//                        .requestMatchers("/trendsetter/**").hasRole("KHACHHANG") // 🚀 Dùng hasRole("KHACHHANG") thay vì hasAuthority("ROLE_KHACHHANG")
//                        .requestMatchers("/admin/thong-ke", "/admin/quan-ly-tai-khoan").hasRole("ADMIN")
//                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "NHANVIEN")
//                        .requestMatchers("/api/user-info").authenticated()
//                        .anyRequest().authenticated()
//                )
//                .exceptionHandling(exception -> exception
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                            response.getWriter().write("Bạn cần đăng nhập để truy cập!");
//                        })
//                        .accessDeniedHandler((request, response, accessDeniedException) -> {
//                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                            response.getWriter().write("Bạn không có quyền truy cập!");
//                        })
//                )
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                .logout(logout -> logout
//                        .logoutUrl("/auth/logout")
//                        .logoutSuccessUrl("/auth/home")
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                        .permitAll()
//                );

        return http.build();
    }

}
