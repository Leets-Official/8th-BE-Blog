package com.leets_8th_be.kanghyeonwoo.auth.adapter.out.kakao;

import static com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError.INVALID_CREDENTIALS;
import static com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError.PROVIDER_UNAVAILABLE;

import com.fasterxml.jackson.databind.JsonNode;
import com.leets_8th_be.kanghyeonwoo.auth.application.model.KakaoIdentity;
import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.KakaoIdentityProvider;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.EmailNormalization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class KakaoClient implements KakaoIdentityProvider {
    private final String clientId;
    private final String secret;
    private final String callback;
    private final RestClient http;

    @Autowired
    public KakaoClient(
            @Value("${auth.kakao.client-id:}") String id,
            @Value("${auth.kakao.client-secret:}") String secret,
            @Value("${auth.kakao.callback:}") String callback,
            RestClient.Builder builder) {
        this(id, secret, callback, configured(builder));
    }

    public KakaoClient(String id, String secret, String callback, RestClient http) {
        this.clientId = id;
        this.secret = secret;
        this.callback = callback;
        this.http = http;
    }

    private static RestClient configured(RestClient.Builder builder) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return builder.requestFactory(factory).build();
    }

    @Override
    public KakaoIdentity identity(String code) {
        if (clientId.isBlank() || callback.isBlank()) {
            throw new AuthenticationFailure(PROVIDER_UNAVAILABLE);
        }
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("redirect_uri", callback);
        form.add("code", code);
        if (!secret.isBlank()) {
            form.add("client_secret", secret);
        }
        try {
            var token = http.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);
            if (token == null || !token.path("access_token").isTextual()) {
                throw new AuthenticationFailure(INVALID_CREDENTIALS);
            }
            var user = http.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .headers(h -> h.setBearerAuth(token.get("access_token").asText()))
                    .retrieve()
                    .body(JsonNode.class);
            if (user == null
                    || !user.path("id").canConvertToLong()
                    || user.path("id").asLong() <= 0) {
                throw new AuthenticationFailure(INVALID_CREDENTIALS);
            }
            var account = user.path("kakao_account");
            String email = null;
            if (account.path("is_email_valid").asBoolean()
                    && account.path("is_email_verified").asBoolean()
                    && account.path("email").isTextual()) {
                email = EmailNormalization.normalize(account.path("email").asText());
            }
            return new KakaoIdentity(user.get("id").asText(), email);
        } catch (RestClientResponseException e) {
            throw new AuthenticationFailure(
                    e.getStatusCode().is4xxClientError() ? INVALID_CREDENTIALS : PROVIDER_UNAVAILABLE);
        } catch (ResourceAccessException e) {
            throw new AuthenticationFailure(PROVIDER_UNAVAILABLE);
        }
    }
}
