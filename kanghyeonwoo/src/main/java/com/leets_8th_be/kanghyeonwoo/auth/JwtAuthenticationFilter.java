package com.leets_8th_be.kanghyeonwoo.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.AuthenticateAccessTokenUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticateAccessTokenUseCase authentication;
    private final ObjectMapper json;

    public JwtAuthenticationFilter(AuthenticateAccessTokenUseCase authentication, ObjectMapper json) {
        this.authentication = authentication;
        this.json = json;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            try {
                if (!header.regionMatches(true, 0, "Bearer ", 0, 7)) {
                    throw new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS);
                }
                var account = authentication.authenticate(header.substring(7));
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(new UsernamePasswordAuthenticationToken(
                        Long.toString(account.userId()),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + account.role()))));
                SecurityContextHolder.setContext(context);
            } catch (AuthenticationFailure failure) {
                SecurityContextHolder.clearContext();
                write(response, 401, ApiResponseCode.UNAUTHORIZED, json);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    static void write(HttpServletResponse response, int status, ApiResponseCode code, ObjectMapper json)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        json.writeValue(response.getOutputStream(), ApiResponse.fail(code, code.getDefaultMessage()));
    }
}
