package com.leets_8th_be.kanghyeonwoo.auth.application;

import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.INVALID_INPUT_VALUE;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.UNAUTHORIZED;

import com.leets_8th_be.kanghyeonwoo.auth.AuthFailure;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Accounts;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Passwords;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Session;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Sessions;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

public class AuthenticationService {
    private final Accounts accounts;
    private final Passwords passwords;
    private final AuthenticationPorts.Tokens jwt;
    private final Sessions sessions;
    private final String dummyHash;

    public AuthenticationService(
            Accounts accounts, Passwords passwords, AuthenticationPorts.Tokens tokens, Sessions sessions) {
        this.accounts = accounts;
        this.passwords = passwords;
        jwt = tokens;
        this.sessions = sessions;
        dummyHash = passwords.encode(UUID.randomUUID().toString());
    }

    public record Tokens(String accessToken, String refreshToken, long expiresIn) {}

    public static String email(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public long signup(String email, String password, String nickname) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new AuthFailure(INVALID_INPUT_VALUE);
        }
        return accounts.register(email(email), nickname.trim(), passwords.encode(password), null)
                .userId();
    }

    public Tokens login(String email, String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new AuthFailure(UNAUTHORIZED);
        }
        var account = accounts.byEmail(email(email)).orElse(null);
        String hash = account == null || account.passwordHash() == null ? dummyHash : account.passwordHash();
        boolean valid = passwords.matches(password, hash);
        if (!valid || account == null || account.passwordHash() == null) {
            throw new AuthFailure(UNAUTHORIZED);
        }
        return start(account.userId());
    }

    public Tokens start(long userId) {
        var session = new Session(UUID.randomUUID().toString(), userId);
        var result = rotate(session);
        sessions.save(session);
        return result;
    }

    public Tokens reissue(String token) {
        var claims = jwt.verify(token, "refresh");
        var session = sessions.lock(claims.sessionId()).orElseThrow(() -> new AuthFailure(UNAUTHORIZED));
        if (session.revoked
                || session.expiresAt.isBefore(Instant.now())
                || !Long.toString(session.userId).equals(Long.toString(claims.userId()))) {
            throw new AuthFailure(UNAUTHORIZED);
        }
        if (!MessageDigest.isEqual(
                session.refreshHash.getBytes(StandardCharsets.UTF_8),
                hash(token).getBytes(StandardCharsets.UTF_8))) {
            session.revoked = true;
            sessions.save(session);
            throw new AuthFailure(UNAUTHORIZED);
        }
        return rotate(session);
    }

    public void logout(String token) {
        var claims = jwt.verify(token, "refresh");
        var session = sessions.lock(claims.sessionId()).orElseThrow(() -> new AuthFailure(UNAUTHORIZED));
        if (!Long.toString(session.userId).equals(Long.toString(claims.userId()))) {
            throw new AuthFailure(UNAUTHORIZED);
        }
        session.revoked = true;
        sessions.save(session);
    }

    private Tokens rotate(Session session) {
        String refresh = jwt.issue(session.userId, "refresh", session.id, Duration.ofDays(7));
        session.refreshHash = hash(refresh);
        session.expiresAt = Instant.now().plus(Duration.ofDays(7));
        sessions.save(session);
        return new Tokens(jwt.issue(session.userId, "access", session.id, Duration.ofMinutes(10)), refresh, 600);
    }

    public static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
