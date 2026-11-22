package com.leets.blog.domain.auth.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

// application.yaml의 kakao.* 값
@ConfigurationProperties(prefix = "kakao")
public record KakaoProperties(
        String clientId,       // REST API 키
        String clientSecret,   // [보안] Client Secret (사용 안 하면 빈 값)
        String redirectUri     // 카카오 디벨로퍼스에 등록한 Redirect URI와 똑같아야 함
) {
}
