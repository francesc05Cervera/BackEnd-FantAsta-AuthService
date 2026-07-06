package com.fantasta.auth_service.security;

import com.fantasta.auth_service.model.Utente;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private javax.crypto.SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Jwts.SIG.HS256.key().build();
    }

    /*
     * Metodo per generare il token JWT
     * @param Utente loggato
     * @return JWT
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

    /*
     * Metodo per validare il token (verifica alterazione)
     * @param JWT
     * @return true se tutto ok altrimenti false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* 
     * Metodo per recuperare l'ID dell'utente dal token
     * @param JWT
     * @return ID Utente
     * 
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.get("userId").toString());
    }
}