package com.flowboard.auth_service;

import com.flowboard.auth_service.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    private static final String VALID_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private JwtServiceImpl jwtService(String secret) {
        JwtServiceImpl svc = new JwtServiceImpl();
        ReflectionTestUtils.setField(svc, "secretKey", secret);
        return svc;
    }

    @Test
    void generateToken_validInputs_returnsParsableJwt() {
        String token = jwtService(VALID_SECRET).generateToken("john@gmail.com", "MEMBER", 1);

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(VALID_SECRET));
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();

        assertNotNull(token);
        assertEquals("john@gmail.com", claims.getSubject());
        assertEquals("MEMBER", claims.get("role"));
        assertEquals(1, ((Number) claims.get("userId")).intValue());
    }

    @Test
    void generateToken_adminRole_claimContainsAdminRole() {
        String token = jwtService(VALID_SECRET).generateToken("admin@gmail.com", "PLATFORM_ADMIN", 2);

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(VALID_SECRET));
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();

        assertEquals("PLATFORM_ADMIN", claims.get("role"));
        assertEquals(2, ((Number) claims.get("userId")).intValue());
    }

//    @Test
//    void generateToken_invalidSecret_throwsException() {
//        assertThrows(IllegalArgumentException.class,
//                () -> jwtService("not-base64!!").generateToken("x@x.com", "MEMBER", 1));
//    }
}