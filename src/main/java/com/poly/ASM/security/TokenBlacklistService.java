package com.poly.ASM.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initializeTable() {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.jwt_tokens') AND type = 'U'",
                Integer.class
        );
        if (exists != null && exists > 0) {
            return;
        }
        jdbcTemplate.execute("""
                CREATE TABLE dbo.jwt_tokens (
                    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                    token_hash VARCHAR(128) NOT NULL UNIQUE,
                    username VARCHAR(50) NULL,
                    token_type VARCHAR(20) NULL,
                    revoked BIT NOT NULL CONSTRAINT DF_jwt_tokens_revoked DEFAULT (0),
                    expires_at DATETIME2 NOT NULL,
                    created_at DATETIME2 NOT NULL CONSTRAINT DF_jwt_tokens_created_at DEFAULT (SYSDATETIME())
                )
                """);
        jdbcTemplate.execute("CREATE INDEX IX_jwt_tokens_username_type ON dbo.jwt_tokens(username, token_type)");
        jdbcTemplate.execute("CREATE INDEX IX_jwt_tokens_expires_at ON dbo.jwt_tokens(expires_at)");
    }

    public void blacklist(String token, Instant expiresAt, String username, String tokenType) {
        if (token == null || token.isBlank() || expiresAt == null) {
            return;
        }
        cleanupExpired();
        String tokenHash = hashToken(token);
        Timestamp expiresAtTimestamp = Timestamp.from(expiresAt);
        int updated = jdbcTemplate.update("""
                UPDATE dbo.jwt_tokens
                SET revoked = 1,
                    expires_at = CASE WHEN expires_at < ? THEN ? ELSE expires_at END,
                    username = COALESCE(username, ?),
                    token_type = COALESCE(token_type, ?)
                WHERE token_hash = ?
                """, expiresAtTimestamp, expiresAtTimestamp, username, tokenType, tokenHash);
        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO dbo.jwt_tokens (token_hash, username, token_type, revoked, expires_at)
                    VALUES (?, ?, ?, 1, ?)
                    """, tokenHash, username, tokenType, expiresAtTimestamp);
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        cleanupExpired();
        String tokenHash = hashToken(token);
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.jwt_tokens
                WHERE token_hash = ?
                  AND revoked = 1
                  AND expires_at > SYSDATETIME()
                """, Integer.class, tokenHash);
        return count != null && count > 0;
    }

    public void storeRefreshToken(String token, String username, Instant expiresAt) {
        if (token == null || token.isBlank() || username == null || username.isBlank() || expiresAt == null) {
            return;
        }
        cleanupExpired();
        String tokenHash = hashToken(token);
        Timestamp expiresAtTimestamp = Timestamp.from(expiresAt);
        int updated = jdbcTemplate.update("""
                UPDATE dbo.jwt_tokens
                SET username = ?,
                    token_type = 'REFRESH',
                    revoked = 0,
                    expires_at = ?
                WHERE token_hash = ?
                """, username, expiresAtTimestamp, tokenHash);
        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO dbo.jwt_tokens (token_hash, username, token_type, revoked, expires_at)
                    VALUES (?, ?, 'REFRESH', 0, ?)
                    """, tokenHash, username, expiresAtTimestamp);
        }
    }

    public boolean isRefreshTokenActive(String token, String username) {
        if (token == null || token.isBlank() || username == null || username.isBlank()) {
            return false;
        }
        cleanupExpired();
        String tokenHash = hashToken(token);
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.jwt_tokens
                WHERE token_hash = ?
                  AND username = ?
                  AND token_type = 'REFRESH'
                  AND revoked = 0
                  AND expires_at > SYSDATETIME()
                """, Integer.class, tokenHash, username);
        return count != null && count > 0;
    }

    private void cleanupExpired() {
        jdbcTemplate.update("DELETE FROM dbo.jwt_tokens WHERE expires_at <= SYSDATETIME()");
    }

    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte value : hash) {
                String hex = Integer.toHexString(0xff & value);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
