package com.fantasta.auth_service.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;

import com.fantasta.auth_service.model.Utente;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTests {
    private static final String SECRET = key(1);

    @Test
    void aSecondInstanceCanVerifyTokensUsingTheSameSecret() {
        var user = new Utente();
        user.setId(42L);
        user.setEmail("mario@example.test");
        String token = provider(SECRET, 60_000).generateToken(user);

        assertEquals(42L, provider(SECRET, 60_000).getUserIdFromToken(token));
        assertThrows(JwtException.class, () -> provider(key(2), 60_000).getUserIdFromToken(token));

        var claims = Jwts.parser()
                .verifyWith(new javax.crypto.spec.SecretKeySpec(Base64.getDecoder().decode(SECRET), "HmacSHA256"))
                .build().parseSignedClaims(token).getPayload();
        assertEquals(60_000L, claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }

    @Test
    void rejectsAnEmptyShortOrMalformedSecretAndNonPositiveExpiry() {
        assertThrows(IllegalStateException.class, () -> provider("", 60_000));
        assertThrows(IllegalStateException.class, () -> provider("YQ==", 60_000));
        assertThrows(IllegalStateException.class, () -> provider("%%%%", 60_000));
        assertThrows(IllegalStateException.class, () -> provider(SECRET, 0));
        assertThrows(IllegalStateException.class, () -> provider(SECRET, -1));
    }

    private static JwtTokenProvider provider(String secret, long expiry) {
        var provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "secret", secret);
        ReflectionTestUtils.setField(provider, "expirationMs", expiry);
        provider.init();
        return provider;
    }

    private static String key(int firstByte) {
        byte[] key = new byte[32];
        key[0] = (byte) firstByte;
        return Base64.getEncoder().encodeToString(key);
    }
}
