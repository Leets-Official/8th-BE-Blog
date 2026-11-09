package com.leets_8th_be.kanghyeonwoo.auth;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "auth_oauth_tickets")
class OAuthTicket {
    @Id String hash;
    String kind;
    String browserHash;
    String kakaoId;
    String email;
    Long userId;
    Instant expiresAt;

    protected OAuthTicket() {}

    OAuthTicket(String hash, String kind, String browserHash, Instant expiresAt) {
        this.hash = hash;
        this.kind = kind;
        this.browserHash = browserHash;
        this.expiresAt = expiresAt;
    }
}
