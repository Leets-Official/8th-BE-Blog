package com.example.demo.controller;

import com.example.demo.auth.KakaoOAuthClient;
import com.example.demo.dto.response.ErrorResponse;
import com.example.demo.dto.response.TokenResponse;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.KakaoLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import java.io.Serializable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static com.example.demo.exception.ErrorCode.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "인증")
public class KakaoAuthController {
    private static final String STATE_KEY = KakaoAuthController.class.getName() + ".state";
    private static final Duration STATE_LIFETIME = Duration.ofMinutes(5);

    private final KakaoOAuthClient kakao;
    private final KakaoLoginService login;
    private final Clock clock;

    @GetMapping("/auth/kakao")
    @Operation(summary = "카카오 로그인 시작", description = """
            브라우저 주소창에서 /auth/kakao를 열면 카카오 로그인 화면으로 이동합니다.
            서버가 현재 브라우저 세션에 5분 동안 유효한 일회용 state 값을 저장합니다.
            로그인 시작부터 콜백까지 같은 브라우저에서 진행하세요.
            """)
    @ApiResponse(responseCode = "302", description = "Location 헤더에 지정된 카카오 인가 화면으로 이동",
            headers = @Header(name = "Location", description = "카카오 인가 요청 URL",
                    schema = @Schema(type = "string", format = "uri")), content = @Content)
    @ApiResponse(responseCode = "503", description = "카카오 로그인 설정 누락",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> startKakaoLogin(HttpServletRequest request, HttpServletResponse response) {
        protectResponse(response);
        String state = UUID.randomUUID().toString();
        var destination = kakao.authorizationUri(state);
        var session = request.getSession();
        synchronized (WebUtils.getSessionMutex(session)) {
            session.setAttribute(STATE_KEY, new LoginState(state, clock.instant().plus(STATE_LIFETIME)));
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(destination).build();
    }

    @GetMapping("/login/oauth2/code/kakao")
    @Operation(summary = "카카오 로그인 콜백", description = """
            브라우저에서 /auth/kakao로 로그인을 시작하면 카카오가 로그인 결과를 이 주소로 전달합니다.
            같은 브라우저 세션의 state를 확인한 뒤 인가 코드를 교환합니다. 처음 로그인하면 서비스 회원을 등록하고,
            다시 로그인하면 연결된 회원에게 서비스용 Access Token과 Refresh Token을 발급합니다.
            """)
    @ApiResponse(responseCode = "200", description = "서비스 토큰 쌍 반환")
    @ApiResponse(responseCode = "400", description = "세션·인가 코드 누락, state 불일치·만료·재사용 또는 로그인 취소",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "카카오가 인가 요청을 거절했거나 연결된 회원이 삭제된 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "502", description = "카카오 서버 응답 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "카카오 로그인 설정 누락",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public TokenResponse completeKakaoLogin(
            @Parameter(description = "로그인 시작 시 발급하고 카카오가 돌려주는 상태 값")
            @RequestParam(required = false) String state,
            @Parameter(description = "카카오 로그인에 성공하면 전달되는 인가 코드")
            @RequestParam(required = false) String code,
            @Parameter(description = "카카오 로그인에 실패하거나 사용자가 취소하면 전달되는 오류 코드")
            @RequestParam(required = false) String error,
            HttpServletRequest request, HttpServletResponse response
    ) {
        protectResponse(response);
        consumeState(request.getSession(false), state);
        if (error != null || !StringUtils.hasText(code)) {
            throw new BusinessException(INVALID_KAKAO_CALLBACK);
        }
        return login.login(kakao.userId(code));
    }

    private void consumeState(HttpSession session, String state) {
        if (session == null) {
            throw new BusinessException(INVALID_KAKAO_CALLBACK);
        }
        synchronized (WebUtils.getSessionMutex(session)) {
            if (!(session.getAttribute(STATE_KEY) instanceof LoginState saved)
                    || !saved.value().equals(state) || !saved.expiresAt().isAfter(clock.instant())) {
                throw new BusinessException(INVALID_KAKAO_CALLBACK);
            }
            session.removeAttribute(STATE_KEY);
        }
    }

    private void protectResponse(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
    }

    private record LoginState(String value, Instant expiresAt) implements Serializable {}
}
