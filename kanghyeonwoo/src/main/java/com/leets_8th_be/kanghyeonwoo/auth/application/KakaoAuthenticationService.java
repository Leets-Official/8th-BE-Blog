package com.leets_8th_be.kanghyeonwoo.auth.application;

import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.*;

import com.leets_8th_be.kanghyeonwoo.auth.AuthFailure;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.*;

import java.security.SecureRandom;
import java.time.*;
import java.util.Base64;

public class KakaoAuthenticationService {
    private final Tickets tickets;
    private final Accounts accounts;
    private final AuthenticationService auth;
    private final Kakao kakao;

    public KakaoAuthenticationService(Tickets t, Accounts a, AuthenticationService auth, Kakao k) {
        tickets = t;
        accounts = a;
        this.auth = auth;
        kakao = k;
    }

    public record Start(String url, String browser) {}

    public record Exchange(
            com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationService.Tokens tokens,
            String signupToken) {}

    public Start start() {
        String browser = random(), state = random();
        String url = kakao.authorize(state);
        save(state, "state", browser, Duration.ofMinutes(5));
        return new Start(url, browser);
    }

    public String callback(String state, String browser, String code) {
        consume(state, "state", browser);
        var identity = kakao.identity(code);
        var existing = accounts.byKakaoId(identity.id());
        if (existing.isEmpty() && (identity.email() == null || identity.email().isBlank()))
            throw new AuthFailure(INVALID_INPUT_VALUE);
        if (existing.isEmpty() && accounts.emailExists(identity.email()))
            throw new AuthFailure(DUPLICATE_RESOURCE);
        String exchange = random();
        var t = save(exchange, "exchange", browser, Duration.ofSeconds(60));
        t.kakaoId = identity.id();
        t.email = identity.email();
        t.userId = existing.map(a -> a.userId()).orElse(null);
        tickets.save(t);
        return kakao.frontend(exchange);
    }

    public Exchange exchange(String code, String browser) {
        var t = consume(code, "exchange", browser);
        if (t.userId != null) return new Exchange(auth.start(t.userId), null);
        String signup = random();
        var next = save(signup, "signup", browser, Duration.ofMinutes(5));
        next.kakaoId = t.kakaoId;
        next.email = t.email;
        tickets.save(next);
        return new Exchange(null, signup);
    }

    public com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationService.Tokens signup(
            String token, String browser, String nickname) {
        var t = consume(token, "signup", browser);
        if (accounts.emailExists(t.email) || accounts.byKakaoId(t.kakaoId).isPresent())
            throw new AuthFailure(DUPLICATE_RESOURCE);
        var account = accounts.register(t.email, nickname.trim(), null, t.kakaoId);
        return auth.start(account.userId());
    }

    private Ticket save(String raw, String kind, String browser, Duration ttl) {
        var t =
                new Ticket(
                        AuthenticationService.hash(raw),
                        kind,
                        AuthenticationService.hash(browser),
                        Instant.now().plus(ttl));
        tickets.save(t);
        return t;
    }

    private Ticket consume(String raw, String kind, String browser) {
        if (raw == null || browser == null) throw new AuthFailure(UNAUTHORIZED);
        var t =
                tickets.lock(AuthenticationService.hash(raw))
                        .orElseThrow(() -> new AuthFailure(UNAUTHORIZED));
        if (!kind.equals(t.kind)
                || !t.browserHash.equals(AuthenticationService.hash(browser))
                || !t.expiresAt.isAfter(Instant.now())) throw new AuthFailure(UNAUTHORIZED);
        tickets.delete(t);
        return t;
    }

    static String random() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
