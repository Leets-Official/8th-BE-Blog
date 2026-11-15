package com.leets.blog.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// 필터 단계(컨트롤러 도달 전) 에러는 @RestControllerAdvice가 받지 못하므로 응답 형식 JSON을 직접 작성
@Component
@RequiredArgsConstructor
public class SecurityErrorResponder {

    private final ObjectMapper objectMapper;

    public void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(errorCode));
    }
}
