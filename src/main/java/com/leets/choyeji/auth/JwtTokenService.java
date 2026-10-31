package com.leets.choyeji.auth;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    public static final long ACCESS_TOKEN_TTL_SECONDS = 3600;
    private final JwtEncoder encoder;
    private final String memberIdClaim;

    public JwtTokenService(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.member-id-claim:sub}") String memberIdClaim) {
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        if (key.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 32 UTF-8 bytes for HS256");
        }
        if (!memberIdClaim.equals("sub") && !memberIdClaim.equals("memberId")) {
            throw new IllegalArgumentException("JWT_MEMBER_ID_CLAIM must be sub or memberId");
        }
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.memberIdClaim = memberIdClaim;
    }

    public String issue(Long memberId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(memberId.toString())
                .claim(memberIdClaim, memberId.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ACCESS_TOKEN_TTL_SECONDS))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
