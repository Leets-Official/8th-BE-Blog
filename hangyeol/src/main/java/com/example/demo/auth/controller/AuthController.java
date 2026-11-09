package com.example.demo.auth.controller;

import com.example.demo.auth.dto.AuthReqDto;
import com.example.demo.auth.dto.AuthResDto;
import com.example.demo.auth.service.AuthService;
import com.example.demo.global.apiPayload.ApiResponse;
import com.example.demo.global.apiPayload.code.GeneralSuccessCode;
import com.example.demo.global.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private static final String REFRESH_COOKIE = "refreshToken";

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping("/auth")
    public ApiResponse<AuthResDto.SignupResponse> signup(
            @Valid @RequestBody AuthReqDto.SignupRequest request) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.SUCCESS_CODE, authService.signup(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResDto.LoginResponse> login(
            @Valid @RequestBody AuthReqDto.LoginRequest request,
            HttpServletResponse response) {

        AuthResDto.TokenResult tokens = authService.login(request);
        addRefreshTokenCookie(response, tokens.refreshToken());

        return ApiResponse.onSuccess(
                GeneralSuccessCode.SUCCESS_CODE,
                new AuthResDto.LoginResponse(tokens.accessToken()));
    }

    @PostMapping("/reissue")
    public ApiResponse<AuthResDto.LoginResponse> reissue(
            @CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {

        AuthResDto.TokenResult tokens = authService.reissue(refreshToken);
        addRefreshTokenCookie(response, tokens.refreshToken());

        return ApiResponse.onSuccess(
                GeneralSuccessCode.SUCCESS_CODE,
                new AuthResDto.LoginResponse(tokens.accessToken()));
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMillis(jwtProvider.getRefreshTokenValidity()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
