package com.leets.blog.security;

import com.leets.blog.dto.auth.TokenResponse;
import com.leets.blog.exception.PostApiException;
import com.leets.blog.repository.UserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
@Profile("mysql")
public class TokenService {
    private static final String ISSUER = "blog-api";
    private final JwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final UserRepository users;

    public TokenService(@Value("${JWT_SECRET}") String secret, UserRepository users) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET은 32바이트 이상이어야 합니다.");
        var key = new SecretKeySpec(bytes, "HmacSHA256");
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO), new JwtIssuerValidator(ISSUER)));
        this.users = users;
    }

    public TokenResponse issue(Long userId) {
        return new TokenResponse(create(userId, "access", 900), create(userId, "refresh", 604800), 900);
    }

    private String create(Long userId, String type, long seconds) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer(ISSUER).subject(userId.toString())
                .issuedAt(now).expiresAt(now.plusSeconds(seconds)).id(UUID.randomUUID().toString())
                .claim("tokenType", type).build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    public Jwt decode(String token, String type) {
        Jwt jwt = decoder.decode(token);
        if (jwt.getExpiresAt() == null || !type.equals(jwt.getClaimAsString("tokenType"))) {
            throw new BadJwtException("토큰 종류 또는 만료 시각이 올바르지 않습니다.");
        }
        try {
            Long id = Long.valueOf(jwt.getSubject());
            if (users.findByIdAndDeletedAtIsNull(id).isEmpty()) throw new BadJwtException("존재하지 않는 사용자입니다.");
        } catch (NumberFormatException e) {
            throw new BadJwtException("사용자 ID가 올바르지 않습니다.", e);
        }
        return jwt;
    }

    public Long refreshUserId(String token) {
        try {
            return Long.valueOf(decode(token, "refresh").getSubject());
        } catch (JwtException e) {
            throw new PostApiException(401, "INVALID_REFRESH_TOKEN", "유효하지 않거나 만료된 Refresh Token입니다.");
        }
    }

    public String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
