package com.sakcadag.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final JwtParser parser;
    private final long expirationMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("app.jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes long");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.parser = Jwts.parser().verifyWith(key).build();
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Optional<String> parseUsername(String token) {
        try {
            return Optional.ofNullable(parser.parseSignedClaims(token).getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
