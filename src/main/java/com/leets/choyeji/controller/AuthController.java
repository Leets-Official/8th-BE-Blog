package com.leets.choyeji.controller;

import com.leets.choyeji.config.ApiErrorResponses;
import com.leets.choyeji.auth.RefreshTokenService;
import com.leets.choyeji.dto.RefreshRequest;
import static com.leets.choyeji.config.SwaggerError.*;
import com.leets.choyeji.dto.ApiResponse;
import com.leets.choyeji.dto.LoginRequest;
import com.leets.choyeji.dto.LoginResponse;
import com.leets.choyeji.dto.SignupRequest;
import com.leets.choyeji.dto.SignupResponse;
import com.leets.choyeji.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "로그인")
    @ApiErrorResponses({INVALID_REQUEST, LOGIN_INPUT, LOGIN_FAILED})
    @PostMapping({"/login", "/auth/login"})
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @Operation(summary = "회원가입")
    @ApiErrorResponses({INVALID_REQUEST, SIGNUP_INPUT, PASSWORD_TOO_LONG, DUPLICATE_EMAIL, DUPLICATE_NICKNAME})
    @PostMapping({"/auth", "/auth/signup"})
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(authService.signup(request));
    }

    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 검증하고 두 토큰을 새로 발급합니다. 기존 Refresh Token은 폐기됩니다.")
    @ApiErrorResponses({INVALID_REQUEST, REFRESH_INPUT, INVALID_REFRESH_TOKEN})
    @PostMapping("/auth/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(refreshTokenService.refresh(request.refreshToken()));
    }
}
