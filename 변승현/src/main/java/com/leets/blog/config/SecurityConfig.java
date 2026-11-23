package com.leets.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets.blog.repository.UserRepository;
import com.leets.blog.security.TokenService;
import com.leets.blog.exception.PostExceptionHandler.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("mysql")
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserRepository users, PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider((UserDetailsService) email -> {
            var user = users.findByEmailAndDeletedAtIsNull(email)
                    .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));
            if (user.getPassword() == null) throw new UsernameNotFoundException("가입 정보가 없습니다.");
            return org.springframework.security.core.userdetails.User.withUsername(email)
                    .password(user.getPassword()).roles("USER").build();
        });
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenService tokens, ObjectMapper mapper) throws Exception {
        var entryPoint = (org.springframework.security.web.AuthenticationEntryPoint) (request, response, error) -> {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            mapper.writeValue(response.getOutputStream(), new ErrorResponse(401, "UNAUTHORIZED", "로그인이 필요하거나 토큰이 유효하지 않습니다."));
        };
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth", "/login", "/auth/refresh", "/string/repeat").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health", "/post/list", "/post/{postId}").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors.authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler((request, response, error) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            mapper.writeValue(response.getOutputStream(), new ErrorResponse(403, "ACCESS_DENIED", "접근 권한이 없습니다."));
                        }))
                .oauth2ResourceServer(resource -> resource.authenticationEntryPoint(entryPoint)
                        .jwt(jwt -> jwt.decoder(token -> tokens.decode(token, "access"))));
        return http.build();
    }
}
