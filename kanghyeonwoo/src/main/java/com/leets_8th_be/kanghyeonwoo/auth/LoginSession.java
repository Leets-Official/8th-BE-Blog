package com.leets_8th_be.kanghyeonwoo.auth;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "auth_login_sessions")
class LoginSession {
    @Id String id;
    long userId;

    @Column(nullable = false, length = 64)
    String refreshHash;

    Instant expiresAt;
    boolean revoked;

    protected LoginSession() {}

    LoginSession(String id, long userId) {
        this.id = id;
        this.userId = userId;
    }
}
