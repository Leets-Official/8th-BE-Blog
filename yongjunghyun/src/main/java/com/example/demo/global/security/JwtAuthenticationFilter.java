package com.example.demo.global.security;

import com.example.demo.global.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// 요청이 Controller에 닿기 전에 한 번 거치는 필터.
// "Authorization: Bearer {토큰}" 헤더가 있으면 검증하고, 성공하면 SecurityContext에 "이 요청은 회원 N번"이라고 적어 둔다.
// 실패해도 여기서 바로 응답하지 않고 기록만 남긴다 → 로그인이 필요한 API라면 EntryPoint가 401로 응답한다.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_ERROR_ATTRIBUTE = "authErrorCode";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                Long memberId = jwtProvider.getMemberIdFromAccessToken(token);
                // principal(주체) 자리에 회원 ID를 넣는다 → Controller에서 @AuthenticationPrincipal Long memberId로 꺼낸다
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        memberId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (CustomException e) {
                request.setAttribute(AUTH_ERROR_ATTRIBUTE, e.getErrorCode());  // 만료인지 위조인지 EntryPoint에 전달
            }
        }

        filterChain.doFilter(request, response);
    }
}
