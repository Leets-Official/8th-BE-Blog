package com.leets_8th_be.kanghyeonwoo.auth.domain.model;

import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationError;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.RefreshTokenReused;
import java.time.Instant;

public final class AuthenticationSession {
    private final String id;
    private final long userId;
    private String refreshHash;
    private Instant expiresAt;
    private boolean revoked;

    public AuthenticationSession(String id, long userId) {
        this(id, userId, null, null, false);
    }

    public AuthenticationSession(String id, long userId, String refreshHash, Instant expiresAt, boolean revoked) {
        this.id = id;
        this.userId = userId;
        this.refreshHash = refreshHash;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public String id() {
        return id;
    }

    public long userId() {
        return userId;
    }

    public String refreshHash() {
        return refreshHash;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public boolean revoked() {
        return revoked;
    }

    public void renew(String hash, Instant expiration) {
        refreshHash = hash;
        expiresAt = expiration;
    }

    public void revoke() {
        revoked = true;
    }

    public void requireOwner(long ownerId) {
        if (userId != ownerId) {
            throw new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS);
        }
    }

    public void requireUsable(long ownerId, Instant now) {
        requireOwner(ownerId);
        if (revoked || expiresAt.isBefore(now)) {
            throw new AuthenticationFailure(AuthenticationError.INVALID_CREDENTIALS);
        }
    }

    public void requireRefreshHash(String hash) {
        if (!TokenFingerprint.matches(refreshHash, hash)) {
            revoke();
            throw new RefreshTokenReused();
        }
    }
}
