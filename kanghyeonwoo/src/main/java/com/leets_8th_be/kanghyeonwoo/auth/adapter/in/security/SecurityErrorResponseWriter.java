package com.leets_8th_be.kanghyeonwoo.auth.adapter.in.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorResponseWriter {
    private final ObjectMapper json;

    public SecurityErrorResponseWriter(ObjectMapper json) {
        this.json = json;
    }

    public void write(HttpServletResponse response, ApiResponseCode code) throws IOException {
        response.setStatus(code.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        json.writeValue(response.getOutputStream(), ApiResponse.fail(code, code.getDefaultMessage()));
    }
}
