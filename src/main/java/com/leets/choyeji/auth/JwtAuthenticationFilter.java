package com.leets.choyeji.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets.choyeji.dto.ApiResponse;
import com.leets.choyeji.entity.Member;
import com.leets.choyeji.exception.ApiException;
import com.leets.choyeji.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtMemberResolver resolver;
    private final MemberRepository members;
    private final ObjectMapper mapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equals(request.getMethod())) return false;
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return switch (path) {
            case "/auth", "/auth/signup", "/login", "/auth/login", "/auth/refresh" -> true;
            default -> false;
        };
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && !authorization.isBlank()) {
            try {
                Long memberId = resolver.resolve(authorization);
                Member member = members.findById(memberId).orElseThrow(ApiException::unauthorized);
                var authentication = new UsernamePasswordAuthenticationToken(memberId, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (ApiException exception) {
                response.setStatus(exception.getStatus().value());
                response.setContentType("application/json;charset=UTF-8");
                mapper.writeValue(response.getWriter(),
                        ApiResponse.failure(exception.getCode(), exception.getMessage(), null));
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
