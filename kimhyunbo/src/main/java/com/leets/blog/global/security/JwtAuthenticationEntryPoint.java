package com.leets.blog.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets.blog.global.exception.ErrorCode;
import com.leets.blog.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Security 필터에서 막힌 요청은 컨트롤러까지 오지 않아서 GlobalExceptionHandler가 처리하지 못한다.
// 그래서 여기서 직접 공통 응답 형식(ApiResponse)으로 401/403을 내려준다.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

    static final String ERROR_CODE_ATTRIBUTE = "authErrorCode";

    private final ObjectMapper objectMapper;

    // 로그인하지 않았거나 토큰이 잘못된 경우 (401)
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException {
        Object errorCode = request.getAttribute(ERROR_CODE_ATTRIBUTE);
        write(response, errorCode instanceof ErrorCode code ? code : ErrorCode.UNAUTHORIZED);
    }

    // 로그인은 했지만 권한이 없는 경우 (403)
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException {
        write(response, ErrorCode.FORBIDDEN);
    }

    private void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(errorCode));
    }
}
