package com.example.shop.config;

import com.example.shop.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        
        // 对于登出接口，完全跳过token验证，确保无论token是否有效都返回成功
        if ("/api/auth/logout".equals(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            
            if (StringUtils.hasText(username) && jwtUtil.isTokenValid(username, token)) {
                setAuthentication(token, username);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void setAuthentication(String token, String username) {
        Integer role = jwtUtil.getRoleFromToken(token);
        String roleName = (role != null && role == 1) ? "ROLE_ADMIN" : "ROLE_USER";
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                username, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
