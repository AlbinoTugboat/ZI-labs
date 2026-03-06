package com.example.informationprotection.service;

import com.example.informationprotection.dto.TokenPairResponse;
import com.example.informationprotection.entity.SessionStatus;
import com.example.informationprotection.entity.User;
import com.example.informationprotection.entity.UserSession;
import com.example.informationprotection.repository.UserRepository;
import com.example.informationprotection.repository.UserSessionRepository;
import com.example.informationprotection.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class TokenPairService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    public TokenPairService(
            JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager,
            UserSessionRepository userSessionRepository,
            UserRepository userRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userSessionRepository = userSessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TokenPairResponse authenticate(String username, String password, String ipAddress, String userAgent) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshToken(refreshToken);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiration())));
        session.setStatus(SessionStatus.ACTIVE);
        userSessionRepository.save(session);

        return new TokenPairResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenPairResponse refreshTokens(String refreshToken, String ipAddress, String userAgent) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        UserSession session = userSessionRepository.findByRefreshTokenAndStatus(refreshToken, SessionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Session not found or inactive"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            userSessionRepository.save(session);
            throw new RuntimeException("Refresh token expired");
        }

        session.setStatus(SessionStatus.REFRESHED);
        session.setRevokedAt(LocalDateTime.now());
        userSessionRepository.save(session);

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        UserSession newSession = new UserSession();
        newSession.setUser(user);
        newSession.setRefreshToken(newRefreshToken);
        newSession.setIpAddress(ipAddress);
        newSession.setUserAgent(userAgent);
        newSession.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiration())));
        newSession.setStatus(SessionStatus.ACTIVE);
        userSessionRepository.save(newSession);

        return new TokenPairResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void revokeSession(String refreshToken) {
        UserSession session = userSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setStatus(SessionStatus.REVOKED);
        session.setRevokedAt(LocalDateTime.now());
        userSessionRepository.save(session);
    }
}

