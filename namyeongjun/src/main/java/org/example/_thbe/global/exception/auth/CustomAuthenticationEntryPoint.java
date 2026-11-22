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
import java.util.LinkedHashMap;
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
        Map<String,Object> body = new LinkedHashMap<>();

        body.put("isSuccess",false);
        body.put("code",code.getCode());
        body.put("message",code.getMessage());
        body.put("data",null);
        objectMapper.writeValue(res.getWriter(),
                body
        );
    }
}
