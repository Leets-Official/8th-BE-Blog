package com.example.demo.global.security;

import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

// 401: 로그인이 필요한 API에 인증 없이(또는 잘못된 토큰으로) 들어온 경우.
// 필터는 Controller 바깥이라 @RestControllerAdvice가 직접 잡지 못한다.
// 그래서 예외를 HandlerExceptionResolver에 넘겨 GlobalExceptionHandler가 같은 ApiResponse 형식으로 응답하게 한다.
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        Object error = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);
        ErrorCode errorCode = (error instanceof ErrorCode code) ? code : ErrorCode.UNAUTHORIZED;
        resolver.resolveException(request, response, null, new CustomException(errorCode));
    }
}
