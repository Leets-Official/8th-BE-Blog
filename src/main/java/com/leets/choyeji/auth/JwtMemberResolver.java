package com.leets.choyeji.auth;

import com.leets.choyeji.exception.ApiException;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class JwtMemberResolver {
    private final NimbusJwtDecoder decoder;
    private final String memberIdClaim;

    public JwtMemberResolver(@Value("${jwt.secret:}") String secret,
                             @Value("${jwt.member-id-claim:sub}") String memberIdClaim) {
        this.memberIdClaim = memberIdClaim;
        if (secret.isBlank()) {
            decoder = null;
        } else {
            byte[] key = secret.getBytes(StandardCharsets.UTF_8);
            if (key.length < 32) {
                throw new IllegalArgumentException("JWT_SECRET must contain at least 32 UTF-8 bytes for HS256");
            }
            decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(key, "HmacSHA256"))
                    .macAlgorithm(MacAlgorithm.HS256).build();
        }
    }

    public Long resolve(String authorization) {
        if (decoder == null || authorization == null || !authorization.startsWith("Bearer ")) {
            throw ApiException.unauthorized();
        }
        try {
            Jwt jwt = decoder.decode(authorization.substring(7));
            if (jwt.getExpiresAt() == null) {
                throw ApiException.unauthorized();
            }
            Object claim = jwt.getClaims().get(memberIdClaim);
            long memberId = Long.parseLong(String.valueOf(claim));
            if (memberId <= 0) {
                throw ApiException.unauthorized();
            }
            return memberId;
        } catch (JwtException | IllegalArgumentException exception) {
            throw ApiException.unauthorized();
        }
    }
}
