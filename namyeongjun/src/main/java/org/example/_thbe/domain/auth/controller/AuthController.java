package org.example._thbe.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.auth.dto.AuthLoginRequest;
import org.example._thbe.domain.auth.dto.AuthLoginResponse;
import org.example._thbe.domain.auth.dto.AuthRefreshResponse;
import org.example._thbe.domain.auth.service.AuthService;
import org.example._thbe.domain.member.dto.MemberCreateRequest;
import org.example._thbe.global.code.SuccessCode;
import org.example._thbe.global.common.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Duration;


@RequestMapping("/auth")
@RequiredArgsConstructor
@Controller
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> createMember(@RequestBody MemberCreateRequest request) {
        authService.createMember(request);
        return ApiResponse.success(SuccessCode.COMMON_CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthLoginResponse>> loginMember(@RequestBody AuthLoginRequest request,
                                                                      HttpServletResponse response) {
        AuthLoginResponse loginResponse = authService.loginMember(request);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.refreshToken())
                .httpOnly(true).secure(true).sameSite("Strict")
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(14))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.success(SuccessCode.COMMON_OK, loginResponse);
    }


    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthRefreshResponse>> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        return ApiResponse.success(SuccessCode.COMMON_OK, authService.refreshToken(refreshToken));
    }

}
