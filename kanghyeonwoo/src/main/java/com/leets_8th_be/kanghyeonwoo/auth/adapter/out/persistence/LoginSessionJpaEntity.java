package com.leets_8th_be.kanghyeonwoo.auth.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity(name = "LoginSession")
@Table(name = "auth_login_sessions")
class LoginSessionJpaEntity {
    @Id
    String id;

    long userId;

    @Column(nullable = false, length = 64)
    String refreshHash;

    Instant expiresAt;
    boolean revoked;

    protected LoginSessionJpaEntity() {}

    LoginSessionJpaEntity(String id, long userId) {
        this.id = id;
        this.userId = userId;
    }
}
