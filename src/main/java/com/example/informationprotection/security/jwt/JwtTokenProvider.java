package com.example.informationprotection.security.jwt;

import com.example.informationprotection.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes();
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Authentication authentication) {
        String username;
        Collection<? extends GrantedAuthority> authorities;

        // Универсальное получение username
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
            authorities = userDetails.getAuthorities();
        } else {
            // Если principal - строка (как в refreshTokens)
            username = authentication.getName();
            authorities = authentication.getAuthorities();
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "ACCESS")
                .claim("roles", roles)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        String username;

        // Универсальное получение username
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        } else {
            username = authentication.getName();
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "REFRESH")
                .claim("tokenId", UUID.randomUUID().toString())
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Основной метод проверки токена
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Проверка access токена
    public boolean validateAccessToken(String token) {
        if (!validateToken(token)) {
            return false;
        }

        Claims claims = getClaimsFromToken(token);
        String tokenType = claims.get("type", String.class);
        return "ACCESS".equals(tokenType);
    }

    // Добавьте этот метод - проверка refresh токена
    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) {
            return false;
        }

        Claims claims = getClaimsFromToken(token);
        String tokenType = claims.get("type", String.class);
        return "REFRESH".equals(tokenType);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaimsFromToken(token);
        String username = claims.getSubject();

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        Collection<GrantedAuthority> authorities = Collections.emptyList();
        if (roles != null) {
            authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username, "", authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("type", String.class);
    }

    public String getTokenId(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("tokenId", String.class);
    }

    // Добавьте этот метод для получения времени истечения refresh токена
    public long getRefreshTokenExpiration() {
        return jwtProperties.getRefreshTokenExpiration();
    }

    // Добавьте геттер для jwtProperties (опционально)
    public JwtProperties getJwtProperties() {
        return jwtProperties;
    }
}
