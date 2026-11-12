package com.leets.mission.controller;

import com.leets.mission.dto.auth.LoginRequest;
import com.leets.mission.dto.auth.RefreshRequest;
import com.leets.mission.dto.auth.SignupRequest;
import com.leets.mission.dto.auth.TokenResponse;
import com.leets.mission.global.exception.ErrorResponse;
import com.leets.mission.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "회원가입, 로그인, 토큰 재발급 API")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "회원가입",
            description = "이메일, 비밀번호, 닉네임으로 회원가입합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이메일 또는 닉네임 중복",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/auth")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호를 검증하고 서비스 토큰을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 정보가 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/login")
    public TokenResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @Operation(
            summary = "토큰 재발급",
            description = "유효한 Refresh Token으로 새 토큰을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 재발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh Token 누락 또는 요청 형식 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않거나 만료된 Refresh Token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/auth/refresh")
    public TokenResponse refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        return authService.refresh(request);
    }
}