package com.leets_8th_be.kanghyeonwoo.auth.adapter.in.web;

import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KakaoRedirects {
    private final String clientId;
    private final String callback;
    private final String frontend;

    public KakaoRedirects(
            @Value("${auth.kakao.client-id:}") String clientId,
            @Value("${auth.kakao.callback:}") String callback,
            @Value("${auth.kakao.frontend:}") String frontend) {
        this.clientId = clientId;
        this.callback = callback;
        this.frontend = frontend;
    }

    public void requireConfigured() {
        if (clientId.isBlank() || callback.isBlank() || frontend.isBlank()) {
            throw new AuthenticationFailure(AuthenticationError.PROVIDER_UNAVAILABLE);
        }
    }

    public URI authorizationUrl(String state) {
        return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", callback)
                .queryParam("state", state)
                .build()
                .encode()
                .toUri();
    }

    public URI completionUrl(String code) {
        return UriComponentsBuilder.fromUriString(frontend)
                .queryParam("code", code)
                .build()
                .encode()
                .toUri();
    }
}
