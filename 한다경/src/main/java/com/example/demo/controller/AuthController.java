package com.example.demo.controller;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RefreshRequest;
import com.example.demo.dto.request.SignupRequest;
import com.example.demo.dto.response.ErrorResponse;
import com.example.demo.dto.response.SignupResponse;
import com.example.demo.dto.response.TokenResponse;
import com.example.demo.service.AuthService;
import com.example.demo.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "인증", description = "이메일 회원가입과 로그인, 카카오 로그인, 서비스 토큰 재발급")
public class AuthController {
    private final AuthService auth;
    private final TokenService tokens;

    @PostMapping(path = "/auth", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "이메일 회원가입",
            description = "이메일, 비밀번호, 닉네임으로 회원을 등록합니다. 가입 후 이메일 로그인에서 토큰을 발급받을 수 있습니다.")
    @ApiResponse(responseCode = "201", description = "가입한 회원의 ID, 이메일, 닉네임 반환")
    @ApiResponse(responseCode = "400", description = "필수값 누락, 형식·길이 위반 또는 잘못된 JSON",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "중복 이메일 또는 닉네임",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "415", description = "Content-Type이 application/json이 아닌 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        return auth.signup(request);
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "이메일 로그인", description = """
            가입한 이메일과 비밀번호를 확인하고 서비스용 Access Token과 Refresh Token을 발급합니다.
            보호 API에는 Authorization 헤더로 Bearer 토큰을 보내세요. Swagger에서는 Authorize에 accessToken 값만 입력합니다.
            """)
    @ApiResponse(responseCode = "200", description = "서비스 토큰 쌍 반환")
    @ApiResponse(responseCode = "400", description = "필수값 누락, 형식·길이 위반 또는 잘못된 JSON",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "등록된 활성 계정이 없거나 비밀번호가 일치하지 않는 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "415", description = "Content-Type이 application/json이 아닌 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(auth.login(request));
    }

    @PostMapping(path = "/auth/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "토큰 재발급", description = """
            로그인 또는 직전 재발급에서 받은 refreshToken을 JSON 본문에 보냅니다.
            Authorization 헤더 없이 호출합니다. 성공하면 기존 Refresh Token이 폐기되므로
            응답으로 받은 accessToken과 refreshToken을 모두 저장하세요. 재발급 가능 기간은 최초 로그인부터 최대 7일입니다.
            """)
    @ApiResponse(responseCode = "200", description = "새 토큰 쌍 발급 성공")
    @ApiResponse(responseCode = "400", description = "필수값 누락, 토큰 길이 위반 또는 잘못된 JSON",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "등록되지 않았거나 만료·사용된 토큰 또는 삭제된 회원의 토큰",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "415", description = "Content-Type이 application/json이 아닌 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(tokens.refresh(request.refreshToken()));
    }
}
