package com.leets.blog.common.security.handler;

import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.common.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 401: 로그인이 필요한 API를 토큰 없이(또는 잘못된 토큰으로) 호출
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorResponder responder;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 필터가 남긴 사유(만료·위조)가 있으면 그 코드, 토큰 자체가 없으면 UNAUTHORIZED
        Object errorCode = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_CODE);
        responder.write(response, errorCode instanceof ErrorCode code ? code : ErrorCode.UNAUTHORIZED);
    }
}
