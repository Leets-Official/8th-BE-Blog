package com.leets.mission.global.security;

import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.equals("/auth")
                || path.equals("/login")
                || path.equals("/auth/refresh");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            Claims claims = jwtProvider.parse(token);

            if (!"ACCESS".equals(claims.get("type", String.class))) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }

            UserDetails user = userDetailsService.loadUserByUsername(claims.getSubject());
            var authentication = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException
                 | IllegalArgumentException
                 | AuthenticationException
                 | CustomException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("Invalid access token", e)
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
