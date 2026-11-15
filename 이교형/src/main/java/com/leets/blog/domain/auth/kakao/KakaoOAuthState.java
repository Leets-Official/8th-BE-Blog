package com.leets.blog.domain.auth.kakao;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

// OAuth state: 로그인 시작 때 만든 값을 쿠키에 두고, 콜백의 state와 같은지 확인 (로그인 CSRF 방지)
// 세션을 쓰지 않으므로 이 브라우저에서 시작한 로그인인지 확인할 곳이 쿠키뿐
@Component
public class KakaoOAuthState {

    public static final String COOKIE_NAME = "kakao_oauth_state";
    private static final String COOKIE_PATH = "/auth/kakao";
    private static final Duration MAX_AGE = Duration.ofMinutes(5);

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // HttpOnly: JS에서 읽지 못함 / SameSite=Lax: 카카오에서 돌아오는 주소창 이동(GET)에는 쿠키가 전달됨
    // 로컬은 http라 Secure는 끔 (배포 시 https면 켜야 함)
    public ResponseCookie cookie(String state) {
        return ResponseCookie.from(COOKIE_NAME, state)
                .httpOnly(true)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(MAX_AGE)
                .build();
    }

    // 콜백 처리 후 쿠키 삭제 (같은 state 재사용 방지)
    public ResponseCookie expiredCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
    }

    public void verify(String savedState, String state) {
        if (!StringUtils.hasText(savedState) || !StringUtils.hasText(state)
                || !MessageDigest.isEqual(savedState.getBytes(StandardCharsets.UTF_8), state.getBytes(StandardCharsets.UTF_8))) {
            throw new CustomException(ErrorCode.KAKAO_LOGIN_FAILED);
        }
    }
}
