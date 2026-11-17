package com.fantasta.auth_service.security;

import com.fantasta.auth_service.model.Utente;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import javax.crypto.spec.SecretKeySpec;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:${jwt.expiration:0}}")
    private long expirationMs;

    private SecretKey key;
    private JwtParser parser;

    /**
     * Builds the signing key from the persistent Base64-encoded secret.
     * The same JWT_SECRET value must be configured in every AuthService instance.
     */
    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("La proprietà jwt.secret deve essere configurata.");
        }

        if (expirationMs <= 0 || expirationMs > Long.MAX_VALUE / 2) {
            throw new IllegalStateException("La proprietà jwt.expiration-ms deve contenere una durata positiva rappresentabile.");
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret.trim());
            if (keyBytes.length < 32) {
                throw new IllegalArgumentException("Chiave JWT troppo corta");
            }
            this.key = new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "jwt.secret deve essere una stringa Base64 valida contenente almeno 32 byte (256 bit).",
                    ex
            );
        }
        parser = Jwts.parser().verifyWith(key).build();
    }

    /**
     * Generates a JWT for the authenticated user.
     */
    public String generateToken(Utente utente) {
        return generateToken(utente, "USER");
    }

    public String generateToken(Utente utente, String role) {
        Date now = new Date();
        Date expiry = new Date(Math.addExact(now.getTime(), expirationMs));

        return Jwts.builder()
                .subject(utente.getEmail())
                .claim("userId", utente.getId())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        var signed = parser.parseSignedClaims(token);
        if (!Jwts.SIG.HS256.getId().equals(signed.getHeader().getAlgorithm())) {
            throw new JwtException("Algoritmo di firma non valido.");
        }
        Claims claims = signed.getPayload();
        Long userId = claims.get("userId", Long.class);
        if (claims.getExpiration() == null || userId == null || userId <= 0) {
            throw new JwtException("Il token deve contenere exp e un userId positivo.");
        }
        return userId;
    }
}
