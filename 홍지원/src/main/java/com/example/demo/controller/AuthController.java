package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.AuthDto;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "회원 인증", description = "회원가입 → 로그인 → 응답의 accessToken을 상단 Authorize에 입력 → 보호 API 호출. 재발급은 refreshToken으로 요청합니다.")
public class AuthController {
    private final AuthService authService;

    // 공개 인증 API는 요청을 검증하고 처리를 Service에 위임합니다.
    @PostMapping("/auth")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입", description = "이메일·닉네임은 중복될 수 없습니다. 가입 시 일반 회원(USER) 권한이 부여됩니다.")
    public ApiResponse<AuthDto.MemberView> signup(@Valid @RequestBody AuthDto.Signup request) {
        return ApiResponse.success("201", authService.signup(request));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "Access Token(15분)과 Refresh Token(7일)을 발급합니다. 재로그인하면 이전 Refresh Token은 폐기됩니다.")
    public ApiResponse<AuthDto.Tokens> login(@Valid @RequestBody AuthDto.Login request) {
        return ApiResponse.success("200", authService.login(request));
    }

    @PostMapping("/auth/refresh")
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새 토큰 쌍을 발급합니다. 사용한 Refresh Token은 폐기되므로 응답의 새 토큰을 사용하세요. Bearer 인증은 필요하지 않습니다.")
    public ApiResponse<AuthDto.Tokens> refresh(@Valid @RequestBody AuthDto.Refresh request) {
        return ApiResponse.success("200", authService.refresh(request));
    }
}
