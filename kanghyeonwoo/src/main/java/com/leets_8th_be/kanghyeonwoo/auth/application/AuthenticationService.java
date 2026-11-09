package com.leets_8th_be.kanghyeonwoo.auth.application;

import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.*;

import com.leets_8th_be.kanghyeonwoo.auth.AuthFailure;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.*;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

public class AuthenticationService {
    private final Accounts accounts;
    private final Passwords passwords;
    private final AuthenticationPorts.Tokens jwt;
    private final Sessions sessions;
    private final String dummyHash;

    public AuthenticationService(
            Accounts a, Passwords p, AuthenticationPorts.Tokens j, Sessions s) {
        accounts = a;
        passwords = p;
        jwt = j;
        sessions = s;
        dummyHash = p.encode(UUID.randomUUID().toString());
    }

    public record Tokens(String accessToken, String refreshToken, long expiresIn) {}

    public static String email(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public long signup(String email, String password, String nickname) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new AuthFailure(INVALID_INPUT_VALUE);
        return accounts.register(email(email), nickname.trim(), passwords.encode(password), null)
                .userId();
    }

    public Tokens login(String email, String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new AuthFailure(UNAUTHORIZED);
        var account = accounts.byEmail(email(email)).orElse(null);
        String hash =
                account == null || account.passwordHash() == null
                        ? dummyHash
                        : account.passwordHash();
        boolean valid = passwords.matches(password, hash);
        if (!valid || account == null || account.passwordHash() == null)
            throw new AuthFailure(UNAUTHORIZED);
        return start(account.userId());
    }

    public Tokens start(long userId) {
        var s = new Session(UUID.randomUUID().toString(), userId);
        var result = rotate(s);
        sessions.save(s);
        return result;
    }

    public Tokens reissue(String token) {
        var claims = jwt.verify(token, "refresh");
        var s = sessions.lock(claims.sessionId()).orElseThrow(() -> new AuthFailure(UNAUTHORIZED));
        if (s.revoked
                || s.expiresAt.isBefore(Instant.now())
                || !Long.toString(s.userId).equals(Long.toString(claims.userId())))
            throw new AuthFailure(UNAUTHORIZED);
        if (!MessageDigest.isEqual(
                s.refreshHash.getBytes(StandardCharsets.UTF_8),
                hash(token).getBytes(StandardCharsets.UTF_8))) {
            s.revoked = true;
            sessions.save(s);
            throw new AuthFailure(UNAUTHORIZED);
        }
        return rotate(s);
    }

    public void logout(String token) {
        var c = jwt.verify(token, "refresh");
        var s = sessions.lock(c.sessionId()).orElseThrow(() -> new AuthFailure(UNAUTHORIZED));
        if (!Long.toString(s.userId).equals(Long.toString(c.userId())))
            throw new AuthFailure(UNAUTHORIZED);
        s.revoked = true;
        sessions.save(s);
    }

    private Tokens rotate(Session s) {
        String refresh = jwt.issue(s.userId, "refresh", s.id, Duration.ofDays(7));
        s.refreshHash = hash(refresh);
        s.expiresAt = Instant.now().plus(Duration.ofDays(7));
        sessions.save(s);
        return new Tokens(
                jwt.issue(s.userId, "access", s.id, Duration.ofMinutes(10)), refresh, 600);
    }

    public static String hash(String value) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
