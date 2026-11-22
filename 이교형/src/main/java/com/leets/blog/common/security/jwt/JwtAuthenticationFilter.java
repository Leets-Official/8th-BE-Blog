package com.leets.blog.common.security.jwt;

import com.leets.blog.common.exception.CustomException;
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

// 요청마다 Authorization: Bearer {accessToken}을 검증해 SecurityContext에 회원 ID와 권한을 저장
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_ERROR_CODE = "authErrorCode";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 토큰이 없으면 인증 없이 통과: 공개 API는 그대로 실행, 보호 API는 EntryPoint가 401
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            try {
                JwtProvider.AccessTokenClaims claims = jwtProvider.parseAccessToken(header.substring(BEARER_PREFIX.length()));
                UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                        claims.memberId(),   // 컨트롤러에서 Long memberId로 받는 값
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + claims.role().name())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (CustomException e) {
                // 바로 401을 쓰지 않고 사유만 남김
                request.setAttribute(AUTH_ERROR_CODE, e.getErrorCode());
            }
        }

        chain.doFilter(request, response);
    }
}
