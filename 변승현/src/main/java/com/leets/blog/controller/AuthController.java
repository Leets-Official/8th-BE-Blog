package com.leets.blog.controller;

import com.leets.blog.dto.auth.*;
import com.leets.blog.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("mysql")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입, 로그인 및 토큰 재발급")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입", description = "이메일과 닉네임은 중복 사용할 수 없습니다.")
    @ApiResponse(responseCode = "201", description = "회원가입 완료")
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "Access Token(15분)과 Refresh Token(7일)을 발급합니다. 재로그인하면 기존 Refresh Token은 만료됩니다.")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/auth/refresh")
    @Operation(summary = "토큰 재발급", description = "Refresh Token을 확인하고 두 토큰을 새로 발급합니다. 사용한 Refresh Token은 다시 사용할 수 없습니다.")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }
}
