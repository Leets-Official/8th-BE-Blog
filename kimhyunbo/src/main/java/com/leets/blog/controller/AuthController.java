package com.leets.blog.controller;

import com.leets.blog.dto.auth.LoginRequest;
import com.leets.blog.dto.auth.ReissueRequest;
import com.leets.blog.dto.auth.SignupRequest;
import com.leets.blog.dto.auth.SignupResponse;
import com.leets.blog.dto.auth.TokenResponse;
import com.leets.blog.global.response.ApiResponse;
import com.leets.blog.global.response.SuccessCode;
import com.leets.blog.global.swagger.ApiErrorCodes;
import com.leets.blog.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.leets.blog.global.exception.ErrorCode.EMAIL_DUPLICATED;
import static com.leets.blog.global.exception.ErrorCode.EXPIRED_TOKEN;
import static com.leets.blog.global.exception.ErrorCode.INVALID_INPUT;
import static com.leets.blog.global.exception.ErrorCode.INVALID_REFRESH_TOKEN;
import static com.leets.blog.global.exception.ErrorCode.INVALID_TOKEN;
import static com.leets.blog.global.exception.ErrorCode.LOGIN_FAILED;
import static com.leets.blog.global.exception.ErrorCode.NICKNAME_DUPLICATED;

@Tag(name = "인증", description = "회원가입, 로그인, 토큰 재발급 API")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 가입합니다. 이미 가입된 이메일이나 사용 중인 닉네임이면 409를 반환합니다.")
    @ApiErrorCodes({INVALID_INPUT, EMAIL_DUPLICATED, NICKNAME_DUPLICATED})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/auth")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return respond(SuccessCode.SIGNUP_COMPLETED, authService.signup(request));
    }

    @Operation(summary = "로그인",
            description = "이메일과 비밀번호로 로그인하고 Access Token, Refresh Token을 발급합니다. "
                    + "로그인이 필요한 API는 요청 헤더에 `Authorization: Bearer {accessToken}`을 넣어 호출합니다.")
    @ApiErrorCodes({INVALID_INPUT, LOGIN_FAILED})
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return respond(SuccessCode.LOGIN_COMPLETED, authService.login(request));
    }

    @Operation(summary = "토큰 재발급",
            description = "Refresh Token으로 새 Access Token과 Refresh Token을 발급합니다. 한 번 사용한 Refresh Token은 다시 쓸 수 없습니다.")
    @ApiErrorCodes({INVALID_INPUT, INVALID_TOKEN, EXPIRED_TOKEN, INVALID_REFRESH_TOKEN})
    @PostMapping("/auth/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody ReissueRequest request) {
        return respond(SuccessCode.TOKEN_REISSUED, authService.reissue(request));
    }

    private <T> ResponseEntity<ApiResponse<T>> respond(SuccessCode successCode, T data) {
        return ResponseEntity.status(successCode.getStatus()).body(ApiResponse.success(successCode, data));
    }
}
