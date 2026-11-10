package com.leets_8th_be.kanghyeonwoo.auth.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity(name = "OAuthTicket")
@Table(name = "auth_oauth_tickets")
class OAuthGrantJpaEntity {
    @Id
    String hash;

    String kind;
    String browserHash;
    String kakaoId;
    String email;
    Long userId;
    Instant expiresAt;

    protected OAuthGrantJpaEntity() {}

    OAuthGrantJpaEntity(String hash, String kind, String browserHash, Instant expiresAt) {
        this.hash = hash;
        this.kind = kind;
        this.browserHash = browserHash;
        this.expiresAt = expiresAt;
    }
}
