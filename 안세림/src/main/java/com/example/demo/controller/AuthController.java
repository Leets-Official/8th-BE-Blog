package com.example.demo.controller;

import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.ReissueRequest;
import com.example.demo.dto.auth.SignupRequest;
import com.example.demo.dto.auth.TokenResponse;
import com.example.demo.dto.member.MemberResponse;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "회원가입, 로그인, 토큰 재발급 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 회원가입합니다. 이메일이나 닉네임이 이미 있으면 409(MEMBER4091, MEMBER4092)를 반환합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("회원가입이 완료되었습니다.", authService.signup(request)));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 Access Token, Refresh Token을 발급합니다. 실패하면 401(AUTH4011)을 반환합니다.")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("로그인에 성공했습니다.", authService.login(request));
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새 Access Token과 Refresh Token을 발급합니다. 이미 사용한 Refresh Token이면 401(AUTH4012)을 반환합니다.")
    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(@Valid @RequestBody ReissueRequest request) {
        return ApiResponse.ok("토큰이 재발급되었습니다.", authService.reissue(request));
    }
}