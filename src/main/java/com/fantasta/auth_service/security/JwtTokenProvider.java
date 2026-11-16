package com.fantasta.auth_service.security;

import com.fantasta.auth_service.model.Utente;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey key;

    /**
     * Builds the signing key from the persistent Base64-encoded secret.
     * The same JWT_SECRET value must be configured in every AuthService instance.
     */
    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("La proprietà jwt.secret deve essere configurata.");
        }

        if (expirationMs <= 0) {
            throw new IllegalStateException("La proprietà jwt.expiration-ms deve essere maggiore di zero.");
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret.trim());
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "jwt.secret deve essere una stringa Base64 valida contenente almeno 32 byte (256 bit).",
                    ex
            );
        }
    }

    /**
     * Generates a JWT for the authenticated user.
     */
    public String generateToken(Utente utente) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(utente.getEmail())
                .claim("userId", utente.getId())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * Validates the JWT signature and expiration.
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Extracts the user ID from a valid JWT.
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object userId = claims.get("userId");
        if (userId == null) {
            throw new IllegalArgumentException("Il token non contiene il claim userId.");
        }

        return Long.valueOf(userId.toString());
    }
}
