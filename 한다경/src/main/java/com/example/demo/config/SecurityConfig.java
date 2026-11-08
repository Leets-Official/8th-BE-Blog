package com.example.demo.config;

import com.example.demo.dto.response.ErrorResponse;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.MemberRepository;
import com.example.demo.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, MemberRepository members, ObjectMapper mapper)
            throws Exception {
        var bearerEntryPoint = new BearerTokenAuthenticationEntryPoint();
        AuthenticationEntryPoint authenticationRequired = (request, response, error) -> {
            bearerEntryPoint.commence(request, response, error);
            writeError(response, mapper, ErrorCode.AUTHENTICATION_REQUIRED);
        };
        return http
                // API credentials are explicitly sent in headers or JSON bodies.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/health", "/posts", "/posts/{postId}",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth", "/login", "/auth/refresh",
                                "/string/repeat").permitAll()
                        .requestMatchers(HttpMethod.POST, "/reports/{reportId}/approve").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(authenticationRequired)
                        .accessDeniedHandler((request, response, error) ->
                                writeError(response, mapper, ErrorCode.ACCESS_DENIED)))
                .oauth2ResourceServer(oauth -> oauth
                        .authenticationEntryPoint(authenticationRequired)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(token -> {
                            var member = members.findByIdAndDeletedAtIsNull(Long.parseLong(token.getSubject()))
                                    .orElseThrow(() -> new InvalidBearerTokenException("Inactive member"));
                            var role = new SimpleGrantedAuthority(member.isAdministrator()
                                    ? "ROLE_ADMIN" : "ROLE_MEMBER");
                            return new JwtAuthenticationToken(token, List.of(role));
                        })))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        String id = "pbkdf2-v1";
        return new DelegatingPasswordEncoder(id,
                Map.of(id, Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()));
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    SecretKey jwtKey(@Value("${blog.auth.jwt-secret}") String secret) {
        byte[] bytes = Base64.getDecoder().decode(secret);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 32 Base64-encoded bytes");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtKey, Clock clock) {
        var decoder = NimbusJwtDecoder.withSecretKey(jwtKey).macAlgorithm(MacAlgorithm.HS256).build();
        var timestamps = new JwtTimestampValidator(Duration.ZERO);
        timestamps.setClock(clock);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                timestamps,
                new JwtIssuerValidator(TokenService.ISSUER),
                new JwtClaimValidator<List<String>>("aud",
                        audiences -> audiences != null && audiences.contains(TokenService.AUDIENCE)),
                jwt -> jwt.getExpiresAt() != null && jwt.getExpiresAt().isAfter(clock.instant())
                        && validMemberId(jwt.getSubject())
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"))));
        return decoder;
    }

    private boolean validMemberId(String subject) {
        try {
            return Long.parseLong(subject) > 0;
        } catch (NumberFormatException error) {
            return false;
        }
    }

    private void writeError(HttpServletResponse response, ObjectMapper mapper, ErrorCode error) throws IOException {
        response.setStatus(error.getStatusValue());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), ErrorResponse.from(error));
    }
}
