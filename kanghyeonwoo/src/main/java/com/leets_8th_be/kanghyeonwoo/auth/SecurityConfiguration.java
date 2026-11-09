package com.leets_8th_be.kanghyeonwoo.auth;

import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.ACCESS_DENIED;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.UNAUTHORIZED;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.AccountDirectory;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@SecurityScheme(name = "BearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SecurityConfiguration {
    private static final String[] PUBLIC_PATHS = {
        "/api/v2/auth/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"
    };

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnWebApplication(type = Type.SERVLET)
    SecurityFilterChain security(HttpSecurity http, JwtTokens jwt, AccountDirectory accounts, ObjectMapper json)
            throws Exception {
        AuthenticationEntryPoint entryPoint =
                (request, response, exception) -> JwtAuthenticationFilter.write(response, 401, UNAUTHORIZED, json);
        AccessDeniedHandler accessDeniedHandler =
                (request, response, exception) -> JwtAuthenticationFilter.write(response, 403, ACCESS_DENIED, json);
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwt, accounts, json);

        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.requestCache(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(authorize -> {
            authorize.requestMatchers(PUBLIC_PATHS).permitAll();
            authorize.requestMatchers(HttpMethod.GET, "/api/v2/reports").hasRole("ADMIN");
            authorize.requestMatchers("/api/v2/reports/*/resolve").hasRole("ADMIN");
            authorize.anyRequest().authenticated();
        });

        http.exceptionHandling(exceptions -> {
            exceptions.authenticationEntryPoint(entryPoint);
            exceptions.accessDeniedHandler(accessDeniedHandler);
        });
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
