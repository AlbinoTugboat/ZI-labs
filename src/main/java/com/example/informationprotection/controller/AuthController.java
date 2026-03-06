package com.example.informationprotection.controller;

import com.example.informationprotection.dto.LoginRequest;
import com.example.informationprotection.dto.RefreshTokenRequest;
import com.example.informationprotection.dto.TokenPairResponse;
import com.example.informationprotection.service.TokenPairService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenPairService tokenPairService;

    public AuthController(TokenPairService tokenPairService) {
        this.tokenPairService = tokenPairService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            TokenPairResponse tokenPair = tokenPairService.authenticate(
                    request.getUsername(),
                    request.getPassword(),
                    getClientIpAddress(httpRequest),
                    httpRequest.getHeader("User-Agent")
            );
            return ResponseEntity.ok(buildTokenResponse(tokenPair, "Authentication successful"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        try {
            TokenPairResponse tokenPair = tokenPairService.refreshTokens(
                    request.getRefreshToken(),
                    getClientIpAddress(httpRequest),
                    httpRequest.getHeader("User-Agent")
            );
            return ResponseEntity.ok(buildTokenResponse(tokenPair, "Tokens refreshed"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    private Map<String, Object> buildTokenResponse(TokenPairResponse tokenPair, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("accessToken", tokenPair.getAccessToken());
        response.put("refreshToken", tokenPair.getRefreshToken());
        response.put("tokenType", "Bearer");
        return response;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}

