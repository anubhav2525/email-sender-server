package com.scheduler.email.security;

import com.scheduler.email.data.entities.auth.AppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    private final SecretKey key;
    private final String issuer;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.access-token-expiry-ms}") long accessTokenExpiryMs,
            @Value("${app.jwt.refresh-token-expiry-ms}") long refreshTokenExpiryMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOKEN GENERATION
    // ─────────────────────────────────────────────────────────────────────────

    public String generateAccessToken(AppUser appUser) {
        return generateToken(appUser, accessTokenExpiryMs);
    }

    public String generateRefreshToken(AppUser appUser) {
        return generateToken(appUser, refreshTokenExpiryMs);
    }

    public Instant refreshTokenExpiry() {
        return Instant.now().plusMillis(refreshTokenExpiryMs);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORE EXTRACTOR
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generic claim extractor.
     * parseClaims() se poora claims object leta hai,
     * phir jo bhi function pass karo woh apply karta hai.
     * <p>
     * Usage:
     * extractClaim(token, Claims::getSubject)       → email
     * extractClaim(token, Claims::getExpiration)    → Date
     * extractClaim(token, c -> c.get("userId"))     → userId string
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SPECIFIC EXTRACTORS — extractClaim ke upar build hue hain
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * User ka email — setSubject() se set kiya tha generateToken() mein
     */
    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Token kab expire hoga
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Token kab issue hua tha
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Issuer nikalo — tumhara app.jwt.issuer value
     */
    public String extractIssuer(String token) {
        return extractClaim(token, Claims::getIssuer);
    }

    /**
     * User ka UUID — generateToken() mein "userId" claim daala tha
     */
    public UUID extractUserId(String token) {
        String userId = extractClaim(token, claims -> claims.get("userId", String.class));
        return UUID.fromString(userId);
    }

    /**
     * Token type — "ACCESS" ya "REFRESH"
     * Blacklist aur validation mein use hota hai
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    /**
     * Koi bhi custom claim nikalo — type safe
     * <p>
     * Usage:
     * extractCustomClaim(token, "userId", String.class)
     * extractCustomClaim(token, "tokenType", String.class)
     */
    public <T> T extractCustomClaim(String token, String claimKey, Class<T> type) {
        return extractClaim(token, claims -> claims.get(claimKey, type));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VALIDATION HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Token expire ho gaya kya?
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Full Access Token validation:
     * Subject match
     * Token expire nahi hua
     * tokenType = ACCESS (refresh token se API call block)
     */
    public boolean isTokenValid(String token, String subject) {
        return subject.equals(extractSubject(token))
                && !isTokenExpired(token)
                && "ACCESS".equals(extractTokenType(token));
    }

    /**
     * Refresh Token validation — /auth/refresh endpoint ke liye:
     * Subject match
     * Token expire nahi hua
     * tokenType = REFRESH (access token se refresh block)
     */
    public boolean isRefreshTokenValid(String token, String subject) {
        return subject.equals(extractSubject(token))
                && !isTokenExpired(token)
                && "REFRESH".equals(extractTokenType(token));
    }

    /**
     * Abhi kitna time bacha hai token mein.
     * Logout blacklist ke liye Redis TTL set karne mein use hota hai.
     * <p>
     * Example:
     * Token 15 min ka tha, 2 min mein logout kiya
     * → 13 min ki Duration return karega
     * → Redis mein 13 min ke liye blacklist karega
     */
    public Duration getRemainingValidity(String token) {
        Date expiry = extractExpiration(token);
        long remainingMillis = expiry.getTime() - System.currentTimeMillis();

        if (remainingMillis <= 0) return Duration.ZERO;

        return Duration.ofMillis(remainingMillis);
    }

    /**
     * Refresh token ka SHA-256 hash — DB mein plain token store nahi karte.
     * DB leak ho bhi jaye → attacker ke paas sirf hash hoga.
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE — Base methods
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Token parse karke poora Claims object return karta hai.
     * Signature verify + expiry check yahi hota hai.
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT malformed: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT unsupported: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT empty or null: {}", e.getMessage());
            throw e;
        }
    }

    private String generateToken(AppUser appUser, long expiryMs) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(appUser.getEmail())
                .claim("userId", appUser.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiryMs)))
                .signWith(key)
                .compact();
    }

}
