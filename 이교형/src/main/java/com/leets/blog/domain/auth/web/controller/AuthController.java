package com.leets.blog.domain.auth.web.controller;

import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.common.response.ApiResponse;
import com.leets.blog.common.swagger.ApiErrorCodes;
import com.leets.blog.domain.auth.kakao.KakaoOAuthState;
import com.leets.blog.domain.auth.service.AuthService;
import com.leets.blog.domain.auth.service.KakaoAuthService;
import com.leets.blog.domain.auth.web.dto.request.LoginRequest;
import com.leets.blog.domain.auth.web.dto.request.ReissueRequest;
import com.leets.blog.domain.auth.web.dto.request.SignUpRequest;
import com.leets.blog.domain.auth.web.dto.response.SignUpResponse;
import com.leets.blog.domain.auth.web.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Auth API", description = "회원가입·로그인·토큰 재발급·카카오 로그인")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoAuthService kakaoAuthService;
    private final KakaoOAuthState kakaoOAuthState;

    @Operation(summary = "회원가입", description = "이메일·비밀번호·닉네임으로 가입합니다. 이메일과 닉네임은 중복될 수 없습니다.")
    @ApiErrorCodes({ErrorCode.REQUIRED_FIELD_MISSING, ErrorCode.INVALID_EMAIL_FORMAT, ErrorCode.INVALID_PASSWORD_FORMAT,
            ErrorCode.INVALID_NICKNAME, ErrorCode.DUPLICATE_EMAIL, ErrorCode.DUPLICATE_NICKNAME})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@RequestBody @Valid SignUpRequest request) {
        return ApiResponse.createdEntity(authService.signUp(request));
    }

    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인해 Access Token과 Refresh Token을 받습니다. "
            + "accessToken을 오른쪽 위 Authorize에 넣으면 자물쇠 API를 호출할 수 있습니다.")
    @ApiErrorCodes({ErrorCode.REQUIRED_FIELD_MISSING, ErrorCode.INVALID_CREDENTIALS})
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.okEntity(authService.login(request));
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새 Access Token과 Refresh Token을 받습니다. "
            + "사용한 Refresh Token은 더 이상 쓸 수 없고, 다시 쓰면 탈취로 보고 로그인 상태를 모두 해제합니다.")
    @ApiErrorCodes({ErrorCode.REQUIRED_FIELD_MISSING, ErrorCode.INVALID_REFRESH_TOKEN})
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@RequestBody @Valid ReissueRequest request) {
        return ApiResponse.okEntity(authService.reissue(request));
    }

    @Operation(summary = "카카오 로그인 시작", description = "state를 쿠키에 저장하고 카카오 로그인 페이지로 이동(302)합니다. "
            + "Swagger에서는 실행되지 않으니 브라우저 주소창에 /auth/kakao/login을 직접 입력하세요.")
    @GetMapping("/kakao/login")
    public ResponseEntity<Void> kakaoLogin() {
        String state = kakaoOAuthState.generate();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, kakaoOAuthState.cookie(state).toString())
                .location(URI.create(kakaoAuthService.getAuthorizeUrl(state)))
                .build();
    }

    @Operation(summary = "카카오 로그인 콜백", description = "카카오가 인가 코드와 state를 붙여 호출합니다. "
            + "처음 로그인한 카카오 계정이면 새 회원으로 등록하고, 서비스 토큰을 발급합니다.")
    @ApiErrorCodes({ErrorCode.KAKAO_LOGIN_FAILED})
    @GetMapping("/kakao/callback")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoCallback(
            @Parameter(description = "카카오가 보내준 인가 코드") @RequestParam(required = false) String code,
            @Parameter(description = "로그인 시작 때 보낸 state") @RequestParam(required = false) String state,
            @Parameter(hidden = true) @CookieValue(name = KakaoOAuthState.COOKIE_NAME, required = false) String savedState,
            HttpServletResponse response) {
        // 성공·실패와 관계없이 state 쿠키는 한 번 쓰고 삭제
        response.addHeader(HttpHeaders.SET_COOKIE, kakaoOAuthState.expiredCookie().toString());
        return ApiResponse.okEntity(kakaoAuthService.login(code, state, savedState));
    }
}
