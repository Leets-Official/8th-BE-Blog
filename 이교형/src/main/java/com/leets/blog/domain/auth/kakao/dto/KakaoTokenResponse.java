package com.leets.blog.domain.auth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// POST https://kauth.kakao.com/oauth/token 응답 중 필요한 값
public record KakaoTokenResponse(
        @JsonProperty("access_token") String accessToken
) {
}
