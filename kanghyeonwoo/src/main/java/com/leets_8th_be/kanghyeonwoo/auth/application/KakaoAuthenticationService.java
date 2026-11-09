package com.leets_8th_be.kanghyeonwoo.auth.application;

import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.DUPLICATE_RESOURCE;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.INVALID_INPUT_VALUE;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.UNAUTHORIZED;

import com.leets_8th_be.kanghyeonwoo.auth.AuthFailure;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Accounts;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Kakao;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Ticket;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Tickets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

public class KakaoAuthenticationService {
    private final Tickets tickets;
    private final Accounts accounts;
    private final AuthenticationService auth;
    private final Kakao kakao;

    public KakaoAuthenticationService(Tickets tickets, Accounts accounts, AuthenticationService auth, Kakao kakao) {
        this.tickets = tickets;
        this.accounts = accounts;
        this.auth = auth;
        this.kakao = kakao;
    }

    public record Start(String url, String browser) {}

    public record Exchange(AuthenticationService.Tokens tokens, String signupToken) {}

    public Start start() {
        String browser = random();
        String state = random();
        String url = kakao.authorize(state);
        save(state, "state", browser, Duration.ofMinutes(5));
        return new Start(url, browser);
    }

    public String callback(String state, String browser, String code) {
        consume(state, "state", browser);
        var identity = kakao.identity(code);
        var existing = accounts.byKakaoId(identity.id());
        if (existing.isEmpty() && (identity.email() == null || identity.email().isBlank())) {
            throw new AuthFailure(INVALID_INPUT_VALUE);
        }
        if (existing.isEmpty() && accounts.emailExists(identity.email())) {
            throw new AuthFailure(DUPLICATE_RESOURCE);
        }
        String exchange = random();
        var ticket = save(exchange, "exchange", browser, Duration.ofSeconds(60));
        ticket.kakaoId = identity.id();
        ticket.email = identity.email();
        ticket.userId = existing.map(account -> account.userId()).orElse(null);
        tickets.save(ticket);
        return kakao.frontend(exchange);
    }

    public Exchange exchange(String code, String browser) {
        var ticket = consume(code, "exchange", browser);
        if (ticket.userId != null) {
            return new Exchange(auth.start(ticket.userId), null);
        }
        String signup = random();
        var next = save(signup, "signup", browser, Duration.ofMinutes(5));
        next.kakaoId = ticket.kakaoId;
        next.email = ticket.email;
        tickets.save(next);
        return new Exchange(null, signup);
    }

    public AuthenticationService.Tokens signup(String token, String browser, String nickname) {
        var ticket = consume(token, "signup", browser);
        if (accounts.emailExists(ticket.email)
                || accounts.byKakaoId(ticket.kakaoId).isPresent()) {
            throw new AuthFailure(DUPLICATE_RESOURCE);
        }
        var account = accounts.register(ticket.email, nickname.trim(), null, ticket.kakaoId);
        return auth.start(account.userId());
    }

    private Ticket save(String raw, String kind, String browser, Duration ttl) {
        var ticket = new Ticket(
                AuthenticationService.hash(raw),
                kind,
                AuthenticationService.hash(browser),
                Instant.now().plus(ttl));
        tickets.save(ticket);
        return ticket;
    }

    private Ticket consume(String raw, String kind, String browser) {
        if (raw == null || browser == null) {
            throw new AuthFailure(UNAUTHORIZED);
        }
        var ticket = tickets.lock(AuthenticationService.hash(raw)).orElseThrow(() -> new AuthFailure(UNAUTHORIZED));
        if (!kind.equals(ticket.kind)
                || !ticket.browserHash.equals(AuthenticationService.hash(browser))
                || !ticket.expiresAt.isAfter(Instant.now())) {
            throw new AuthFailure(UNAUTHORIZED);
        }
        tickets.delete(ticket);
        return ticket;
    }

    static String random() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
