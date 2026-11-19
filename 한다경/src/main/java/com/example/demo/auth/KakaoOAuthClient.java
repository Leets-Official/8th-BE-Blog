package com.example.demo.auth;

import com.example.demo.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

import static com.example.demo.exception.ErrorCode.*;

@Component
public class KakaoOAuthClient {
    private static final String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestClient http;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    @Autowired
    public KakaoOAuthClient(RestClient.Builder builder,
                            @Value("${blog.auth.kakao.client-id}") String clientId,
                            @Value("${blog.auth.kakao.client-secret}") String clientSecret,
                            @Value("${blog.auth.kakao.redirect-uri}") String redirectUri) {
        this(builder.requestFactory(requestFactory()).build(), clientId, clientSecret, redirectUri);
    }

    KakaoOAuthClient(RestClient http, String clientId, String clientSecret, String redirectUri) {
        this.http = http;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public URI authorizationUri(String state) {
        requireConfiguration();
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", "{clientId}")
                .queryParam("redirect_uri", "{redirectUri}")
                .queryParam("state", "{state}")
                .encode().buildAndExpand(clientId, redirectUri, state).toUri();
    }

    public long userId(String code) {
        requireConfiguration();
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        try {
            KakaoToken token = http.post().uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form).retrieve().body(KakaoToken.class);
            if (token == null || !StringUtils.hasText(token.accessToken())) {
                throw new BusinessException(KAKAO_PROVIDER_ERROR);
            }

            KakaoUser user = http.get().uri(USER_URL)
                    .headers(headers -> headers.setBearerAuth(token.accessToken()))
                    .retrieve().body(KakaoUser.class);
            if (user == null || user.id() == null || user.id() <= 0) {
                throw new BusinessException(KAKAO_PROVIDER_ERROR);
            }
            return user.id();
        } catch (RestClientResponseException error) {
            if (error.getStatusCode().is4xxClientError() && error.getStatusCode().value() != 429) {
                throw new BusinessException(KAKAO_LOGIN_FAILED);
            }
            throw new BusinessException(KAKAO_PROVIDER_ERROR);
        } catch (RestClientException error) {
            throw new BusinessException(KAKAO_PROVIDER_ERROR);
        }
    }

    private void requireConfiguration() {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)
                || !StringUtils.hasText(redirectUri)) {
            throw new BusinessException(KAKAO_NOT_CONFIGURED);
        }
    }

    private static JdkClientHttpRequestFactory requestFactory() {
        var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(5)).build();
        var factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(5));
        return factory;
    }

    private record KakaoToken(@JsonProperty("access_token") String accessToken) {}
    private record KakaoUser(Long id) {}
}
