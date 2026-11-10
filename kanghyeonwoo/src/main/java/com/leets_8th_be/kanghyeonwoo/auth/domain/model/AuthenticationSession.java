package com.leets_8th_be.kanghyeonwoo.auth.domain.model;

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
}
