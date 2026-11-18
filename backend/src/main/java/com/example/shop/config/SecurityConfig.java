package com.example.shop.config;

import com.example.shop.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .exceptionHandling()
            .authenticationEntryPoint((request, response, authException) -> {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(401);
                response.getWriter().write("{\"code\":401,\"message\":\"未授权，请登录\"}");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(403);
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
            })
            .and()
            .authorizeRequests()
            .antMatchers("/api/auth/login", "/api/auth/register", "/api/auth/logout").permitAll()
            .antMatchers("/api/auth/registration-code", "/api/registration-test/**").permitAll()
            .antMatchers("/api/product/**").permitAll()
            .antMatchers("/api/search/**").permitAll()
            .antMatchers("/api/banner/**").permitAll()
            .antMatchers("/api/captcha/enabled").permitAll()
            .antMatchers("/api/captcha/generate").permitAll()
            .antMatchers("/api/captcha/verify").permitAll()
            .antMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
            .antMatchers("/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
            .antMatchers("/swagger-resources/**", "/webjars/**").permitAll()
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
