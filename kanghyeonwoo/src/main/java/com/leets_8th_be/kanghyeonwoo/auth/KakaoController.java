package com.leets_8th_be.kanghyeonwoo.auth;

import com.leets_8th_be.kanghyeonwoo.auth.adapter.in.web.KakaoRedirects;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.Exchange;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.CompleteKakaoLoginUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.ExchangeKakaoCodeUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.SignupWithKakaoUseCase;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.in.StartKakaoLoginUseCase;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/auth/kakao")
public class KakaoController {
    private final StartKakaoLoginUseCase startLogin;
    private final CompleteKakaoLoginUseCase completeLogin;
    private final ExchangeKakaoCodeUseCase exchangeCode;
    private final SignupWithKakaoUseCase signup;
    private final KakaoRedirects redirects;

    public KakaoController(
            StartKakaoLoginUseCase startLogin,
            CompleteKakaoLoginUseCase completeLogin,
            ExchangeKakaoCodeUseCase exchangeCode,
            SignupWithKakaoUseCase signup,
            KakaoRedirects redirects) {
        this.startLogin = startLogin;
        this.completeLogin = completeLogin;
        this.exchangeCode = exchangeCode;
        this.signup = signup;
        this.redirects = redirects;
    }

    @ApiErrorCodes({
        ApiResponseCode.UNAUTHORIZED,
        ApiResponseCode.INVALID_INPUT_VALUE,
        ApiResponseCode.DUPLICATE_RESOURCE,
        ApiResponseCode.OAUTH_UNAVAILABLE
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "302",
            description = "성공",
            useReturnTypeSchema = true)
    @GetMapping("/start")
    public ResponseEntity<Void> start(HttpServletRequest req) {
        redirects.requireConfigured();
        var start = startLogin.start();
        var cookie = ResponseCookie.from("kakao_browser", start.browser())
                .httpOnly(true)
                .secure(req.isSecure())
                .sameSite("Lax")
                .path("/api/v2/auth/kakao")
                .maxAge(600)
                .build();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .location(redirects.authorizationUrl(start.state()))
                .build();
    }

    @ApiErrorCodes({
        ApiResponseCode.UNAUTHORIZED,
        ApiResponseCode.INVALID_INPUT_VALUE,
        ApiResponseCode.DUPLICATE_RESOURCE,
        ApiResponseCode.OAUTH_UNAVAILABLE
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "302",
            description = "성공",
            useReturnTypeSchema = true)
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam String state,
            @RequestParam String code,
            @CookieValue(name = "kakao_browser", required = false) String browser) {
        String exchange = completeLogin.complete(state, browser, code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(redirects.completionUrl(exchange))
                .build();
    }

    public record Code(@NotBlank String code) {}

    public record Signup(
            @NotBlank String signupToken,
            @NotBlank @Size(max = 50) String nickname) {
        public Signup {
            if (nickname != null) {
                nickname = nickname.trim();
            }
        }
    }

    @ApiErrorCodes({
        ApiResponseCode.UNAUTHORIZED,
        ApiResponseCode.INVALID_INPUT_VALUE,
        ApiResponseCode.DUPLICATE_RESOURCE,
        ApiResponseCode.OAUTH_UNAVAILABLE
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            useReturnTypeSchema = true)
    @PostMapping("/exchange")
    public ApiResponse<Exchange> exchange(
            @Valid @RequestBody Code request, @CookieValue(name = "kakao_browser", required = false) String browser) {
        Exchange exchange = exchangeCode.exchange(request.code(), browser);
        return ApiResponse.success(exchange);
    }

    @ApiErrorCodes({
        ApiResponseCode.UNAUTHORIZED,
        ApiResponseCode.INVALID_INPUT_VALUE,
        ApiResponseCode.DUPLICATE_RESOURCE,
        ApiResponseCode.OAUTH_UNAVAILABLE
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            useReturnTypeSchema = true)
    @PostMapping("/signup")
    public ApiResponse<Tokens> signup(
            @Valid @RequestBody Signup request, @CookieValue(name = "kakao_browser", required = false) String browser) {
        Tokens tokens = signup.signup(request.signupToken(), browser, request.nickname());
        return ApiResponse.success(tokens);
    }
}
