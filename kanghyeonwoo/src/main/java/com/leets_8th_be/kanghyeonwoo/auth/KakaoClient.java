package com.leets_8th_be.kanghyeonwoo.auth;

import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.*;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KakaoClient {
    private final String clientId, secret, callback, frontend;
    private final RestClient http;

    @org.springframework.beans.factory.annotation.Autowired
    public KakaoClient(
            @Value("${auth.kakao.client-id:}") String id,
            @Value("${auth.kakao.client-secret:}") String secret,
            @Value("${auth.kakao.callback:}") String callback,
            @Value("${auth.kakao.frontend:}") String frontend,
            RestClient.Builder builder) {
        this(id, secret, callback, frontend, configured(builder));
    }

    KakaoClient(String id, String secret, String callback, String frontend, RestClient http) {
        this.clientId = id;
        this.secret = secret;
        this.callback = callback;
        this.frontend = frontend;
        this.http = http;
    }

    private static RestClient configured(RestClient.Builder builder) {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return builder.requestFactory(factory).build();
    }

    public String authorize(String state) {
        if (clientId.isBlank() || callback.isBlank() || frontend.isBlank())
            throw new AuthFailure(OAUTH_UNAVAILABLE);
        return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", callback)
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }

    public String frontend(String code) {
        return UriComponentsBuilder.fromUriString(frontend)
                .queryParam("code", code)
                .build()
                .encode()
                .toUriString();
    }

    public record Identity(String id, String email) {}

    public Identity identity(String code) {
        if (clientId.isBlank() || callback.isBlank()) throw new AuthFailure(OAUTH_UNAVAILABLE);
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("redirect_uri", callback);
        form.add("code", code);
        if (!secret.isBlank()) form.add("client_secret", secret);
        try {
            var token =
                    http.post()
                            .uri("https://kauth.kakao.com/oauth/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .body(form)
                            .retrieve()
                            .body(JsonNode.class);
            if (token == null || !token.path("access_token").isTextual())
                throw new AuthFailure(UNAUTHORIZED);
            var user =
                    http.get()
                            .uri("https://kapi.kakao.com/v2/user/me")
                            .headers(h -> h.setBearerAuth(token.get("access_token").asText()))
                            .retrieve()
                            .body(JsonNode.class);
            if (user == null
                    || !user.path("id").canConvertToLong()
                    || user.path("id").asLong() <= 0) throw new AuthFailure(UNAUTHORIZED);
            var account = user.path("kakao_account");
            String email = null;
            if (account.path("is_email_valid").asBoolean()
                    && account.path("is_email_verified").asBoolean()
                    && account.path("email").isTextual())
                email = AuthService.email(account.path("email").asText());
            return new Identity(user.get("id").asText(), email);
        } catch (org.springframework.web.client.RestClientResponseException e) {
            throw new AuthFailure(
                    e.getStatusCode().is4xxClientError() ? UNAUTHORIZED : OAUTH_UNAVAILABLE);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            throw new AuthFailure(OAUTH_UNAVAILABLE);
        }
    }
}
