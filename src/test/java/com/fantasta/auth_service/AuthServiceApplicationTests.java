package com.fantasta.auth_service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;

import com.fantasta.auth_service.model.Utente;
import com.fantasta.auth_service.repository.UtenteRepository;
import com.fantasta.auth_service.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=${TEST_DB_URL:jdbc:h2:mem:auth-test}",
        "spring.datasource.username=${TEST_DB_USERNAME:sa}",
        "spring.datasource.password=${TEST_DB_PASSWORD:}",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=" + AuthServiceApplicationTests.SECRET,
        "jwt.expiration=60000"
})
@AutoConfigureMockMvc
@Transactional
class AuthServiceApplicationTests {

    static final String SECRET = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    private static final String EMAIL = "mario@example.test";
    private static final String PASSWORD = "password-123";
    private static final String REGISTER = """
            {"email":"mario@example.test","name":"Mario","surname":"Rossi","password":"password-123"}
            """;

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UtenteRepository users;
    @Autowired JwtTokenProvider jwt;

    @Test
    void registrationLoginAndMePreserveTheHttpContract() throws Exception {
        mvc.perform(post("/api/user/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.accessToken").doesNotExist());
        mvc.perform(post("/api/user/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
                .andExpect(status().isConflict());

        String body = mvc.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(body).required("accessToken").asText();
        mvc.perform(get("/api/user/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(users.findByEmail(EMAIL).getId()))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.name").value("Mario"))
                .andExpect(jsonPath("$.surname").value("Rossi"));
    }

    @Test
    void missingMalformedExpiredAndInvalidClaimsReturnUnauthorized() throws Exception {
        var key = new SecretKeySpec(Base64.getDecoder().decode(SECRET), "HmacSHA256");
        var future = Date.from(Instant.now().plusSeconds(60));
        String expired = Jwts.builder().claim("userId", 1)
                .expiration(Date.from(Instant.now().minusSeconds(60))).signWith(key).compact();
        String noExpiry = Jwts.builder().claim("userId", 1).signWith(key).compact();
        String noId = Jwts.builder().expiration(future).signWith(key).compact();
        String stringId = Jwts.builder().claim("userId", "1").expiration(future).signWith(key).compact();
        String wrongKey = Jwts.builder().claim("userId", 1).expiration(future)
                .signWith(Jwts.SIG.HS256.key().build()).compact();

        mvc.perform(get("/api/user/me")).andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
        for (String value : new String[] {"Basic example", "Bearer ", "Bearer malformed",
                "Bearer " + expired, "Bearer " + noExpiry, "Bearer " + noId,
                "Bearer " + stringId, "Bearer " + wrongKey}) {
            mvc.perform(get("/api/user/me").header(HttpHeaders.AUTHORIZATION, value))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void validTokenForDeletedUserReturnsNotFound() throws Exception {
        Utente user = new Utente();
        user.setId(9999999L);
        user.setEmail(EMAIL);
        mvc.perform(get("/api/user/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.generateToken(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void validatesRequestBodiesAndExposesPublicHealthCheck() throws Exception {
        mvc.perform(get("/api/auth/ping")).andExpect(status().isOk());
        for (String endpoint : new String[] {"login", "register"}) {
            mvc.perform(post("/api/user/" + endpoint).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
