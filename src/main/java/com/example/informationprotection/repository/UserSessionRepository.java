package com.example.informationprotection.repository;

import com.example.informationprotection.entity.SessionStatus;
import com.example.informationprotection.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    Optional<UserSession> findByRefreshTokenAndStatus(String refreshToken, SessionStatus status);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = :status, s.revokedAt = :revokedAt WHERE s.refreshToken = :refreshToken")
    int revokeSession(@Param("refreshToken") String refreshToken,
                      @Param("status") SessionStatus status,
                      @Param("revokedAt") LocalDateTime revokedAt);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'EXPIRED' WHERE s.expiresAt < :now AND s.status = 'ACTIVE'")
    int expireSessions(@Param("now") LocalDateTime now);
}
