package com.leets.blog.domain.auth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// GET https://kapi.kakao.com/v2/user/me 응답 중 필요한 값 (모르는 필드는 스프링 기본 설정이 무시함)
public record KakaoUserResponse(
        Long id,   // 카카오 회원번호
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {

    public record KakaoAccount(
            String email,
            @JsonProperty("is_email_verified") Boolean isEmailVerified,
            Profile profile
    ) {
    }

    public record Profile(String nickname) {
    }

    // 카카오에서 인증된 이메일만 사용 (동의 안 했거나 인증 안 된 이메일이면 null)
    public String verifiedEmail() {
        if (kakaoAccount == null || kakaoAccount.email() == null) {
            return null;
        }
        return Boolean.TRUE.equals(kakaoAccount.isEmailVerified()) ? kakaoAccount.email() : null;
    }

    public String nickname() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().nickname();
    }
}
