package io.github.appleaww.messenger.security;

import io.github.appleaww.messenger.model.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.EcPrivateJwk;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.access:CHANGE_ME_IN_PROPERTIES}")
    private String jwtAccessSignature; // часть JWT токена(секретный ключ для подписи)

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration; // время жизни токена в миллисекундах

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(jwtExpiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().toString())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;

        } catch (Exception e) {
            log.warn("Invalid JWT token");
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public long extractId(String token) {
        return Long.parseLong(Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
    }

    public String extractRole(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
    public String extractEmail(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }


    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtAccessSignature.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA512"); // массив, от, до, алгоритм
    }


}
