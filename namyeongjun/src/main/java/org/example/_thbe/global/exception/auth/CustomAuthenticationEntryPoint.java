package org.example._thbe.global.exception.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.auth.error.AuthErrorCode;
import org.example._thbe.global.jwt.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        String reason = (String) request.getAttribute(JwtUtil.JWT_ERROR);
        AuthErrorCode code = "TOKEN_EXPIRED".equals(reason) ? AuthErrorCode.TOKEN_EXPIRED
                : AuthErrorCode.TOKEN_INVALID;
        write(response, code);
    }

    private void write(HttpServletResponse res, AuthErrorCode code) throws IOException {
        res.setStatus(code.getStatus().value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        objectMapper
                .writeValue(
                        res.getWriter(),
                        Map.of("isSuccess",false,
                                "code", code.getCode(),
                                "message", code.getMessage(),
                                "data", null
                        )
                );
    }
}
