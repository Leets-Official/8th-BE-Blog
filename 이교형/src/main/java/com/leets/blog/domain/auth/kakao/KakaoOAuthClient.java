package com.leets.blog.domain.auth.kakao;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.auth.kakao.dto.KakaoTokenResponse;
import com.leets.blog.domain.auth.kakao.dto.KakaoUserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.function.Supplier;

// 카카오 서버 호출만 담당 (테스트에서는 이 클래스를 Mock으로 바꿔 카카오 없이 검증)
@Slf4j
@Component
public class KakaoOAuthClient {

    private static final String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    // 기본 RestClient에는 읽기 타임아웃이 없어, 카카오가 응답하지 않으면 요청 스레드가 계속 기다림
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final KakaoProperties properties;
    private final RestClient restClient;

    public KakaoOAuthClient(KakaoProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
    }

    // 1) 사용자를 보낼 카카오 로그인 페이지 주소 (state는 콜백에서 그대로 돌려받아 검증)
    public String getAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("client_id", properties.clientId())
                .queryParam("redirect_uri", properties.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .encode()
                .build()
                .toUriString();
    }

    // 2) 콜백으로 받은 인가 코드 → 카카오 Access Token
    public String getAccessToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.clientId());
        form.add("redirect_uri", properties.redirectUri());
        form.add("code", code);
        if (StringUtils.hasText(properties.clientSecret())) {
            form.add("client_secret", properties.clientSecret());
        }

        KakaoTokenResponse response = call(() -> restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KakaoTokenResponse.class));

        if (response == null || response.accessToken() == null) {
            throw new CustomException(ErrorCode.KAKAO_LOGIN_FAILED);
        }
        return response.accessToken();
    }

    // 3) 카카오 Access Token → 카카오 사용자 정보
    public KakaoUserResponse getUserInfo(String kakaoAccessToken) {
        KakaoUserResponse response = call(() -> restClient.get()
                .uri(USER_INFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
                .retrieve()
                .body(KakaoUserResponse.class));

        if (response == null || response.id() == null) {
            throw new CustomException(ErrorCode.KAKAO_LOGIN_FAILED);
        }
        return response;
    }

    // 카카오 4xx·5xx(인가 코드 재사용 KOE320, Redirect URI 불일치 KOE006 등), 연결 실패, 타임아웃,
    // 응답 변환 실패가 모두 RestClientException → KAKAO_LOGIN_FAILED (그대로 두면 500)
    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientException e) {
            log.warn("카카오 API 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.KAKAO_LOGIN_FAILED);
        }
    }
}
