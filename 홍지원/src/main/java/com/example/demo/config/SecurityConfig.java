package com.example.demo.config;

import com.example.demo.domain.member.MemberRepository;
import com.example.demo.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "로그인 응답의 accessToken만 입력하세요. Bearer 접두사는 자동으로 붙습니다.")
public class SecurityConfig {
    // 쿠키 세션 없이 Bearer 토큰으로 인증하므로 CSRF와 세션 생성을 사용하지 않습니다.
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, MemberRepository members, ObjectMapper mapper) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth", "/login", "/auth/refresh", "/string/repeat").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health", "/posts", "/posts/*").permitAll()
                        .requestMatchers("/reports", "/reports/**", "/posts/*/visibility").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(token -> {
                    // 토큰의 회원이 삭제되었는지 확인하고 DB의 최신 권한을 적용합니다.
                    long memberId;
                    try {
                        memberId = Long.parseLong(token.getSubject());
                    } catch (RuntimeException exception) {
                        throw new OAuth2AuthenticationException("invalid_token");
                    }
                    var member = members.findById(memberId).filter(m -> m.getDeletedAt() == null)
                            .orElseThrow(() -> new OAuth2AuthenticationException("invalid_token"));
                    return new JwtAuthenticationToken(token,
                            List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole())), token.getSubject());
                })).authenticationEntryPoint((request, response, exception) -> writeError(mapper, response, 401))
                        .accessDeniedHandler((request, response, exception) -> writeError(mapper, response, 403)))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> writeError(mapper, response, 401))
                        .accessDeniedHandler((request, response, exception) -> writeError(mapper, response, 403)))
                .build();
    }

    // 서명 키는 코드에 보관하지 않고 환경 변수에서 받습니다.
    @Bean
    SecretKey jwtKey(@Value("${jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET은 32바이트 이상이어야 합니다.");
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtKey).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("blog"));
        return decoder;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 필터에서 발생한 인증·인가 오류도 기존 API 응답 형식으로 반환합니다.
    private void writeError(ObjectMapper mapper, HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
        if (status == 401) response.setHeader("WWW-Authenticate", "Bearer");
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), ApiResponse.failure(status,
                status == 401 ? "UNAUTHORIZED" : "FORBIDDEN",
                status == 401 ? "유효한 Access Token이 필요합니다." : "접근 권한이 없습니다."));
    }
}
