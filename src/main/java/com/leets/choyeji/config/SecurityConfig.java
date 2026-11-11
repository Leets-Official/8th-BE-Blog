package com.leets.choyeji.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets.choyeji.auth.JwtAuthenticationFilter;
import com.leets.choyeji.auth.JwtMemberResolver;
import com.leets.choyeji.dto.ApiResponse;
import com.leets.choyeji.exception.ApiException;
import com.leets.choyeji.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtMemberResolver resolver,
                                            MemberRepository members, ObjectMapper mapper) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .requestCache(cache -> cache.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth", "/auth/signup", "/login", "/auth/login",
                                "/auth/refresh", "/string/repeat").permitAll()
                        .requestMatchers("/health", "/error", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts", "/posts/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, mapper, ApiException.unauthorized()))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(response, mapper, ApiException.adminRequired())))
                .addFilterBefore(new JwtAuthenticationFilter(resolver, members, mapper),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private static void writeError(HttpServletResponse response, ObjectMapper mapper,
                                   ApiException exception) throws IOException {
        response.setStatus(exception.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(),
                ApiResponse.failure(exception.getCode(), exception.getMessage(), null));
    }
}
