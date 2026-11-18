package com.example.shop.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    private static final Map<String, String> tokenCache = new HashMap<>();
    
    public String generateToken(String username) {
        return generateToken(username, 0);
    }
    
    public String generateToken(String username, Integer role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        String token = Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
        
        tokenCache.put(username, token);
        return token;
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
    
    public Integer getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        Object role = claims.get("role");
        if (role instanceof Integer) {
            return (Integer) role;
        }
        return 0;
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void invalidateToken(String username) {
        tokenCache.remove(username);
    }
    
    public boolean isTokenValid(String username, String token) {
        String cachedToken = tokenCache.get(username);
        return token != null && token.equals(cachedToken);
    }
}
