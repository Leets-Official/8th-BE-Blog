package com.leets_8th_be.kanghyeonwoo.auth.application;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public final class AuthenticationPorts {
    private AuthenticationPorts() {}

    public record Account(long userId, String passwordHash, String kakaoId, String role) {}

    public interface Accounts {
        Optional<Account> byEmail(String email);

        Optional<Account> byKakaoId(String id);

        boolean emailExists(String email);

        Account register(String email, String nickname, String hash, String kakaoId);
    }

    public interface Passwords {
        String encode(String raw);

        boolean matches(String raw, String hash);
    }

    public record Claims(long userId, String sessionId) {}

    public interface Tokens {
        String issue(long userId, String type, String session, Duration ttl);

        Claims verify(String raw, String type);
    }

    public static final class Session {
        public String id;
        public long userId;
        public String refreshHash;
        public Instant expiresAt;
        public boolean revoked;

        public Session(String id, long userId) {
            this.id = id;
            this.userId = userId;
        }
    }

    public interface Sessions {
        Optional<Session> lock(String id);

        void save(Session session);
    }

    public static final class Ticket {
        public String hash;
        public String kind;
        public String browserHash;
        public String kakaoId;
        public String email;
        public Long userId;
        public Instant expiresAt;

        public Ticket(String hash, String kind, String browserHash, Instant expiresAt) {
            this.hash = hash;
            this.kind = kind;
            this.browserHash = browserHash;
            this.expiresAt = expiresAt;
        }
    }

    public interface Tickets {
        Optional<Ticket> lock(String hash);

        void save(Ticket ticket);

        void delete(Ticket ticket);
    }

    public record Identity(String id, String email) {}

    public interface Kakao {
        String authorize(String state);

        String frontend(String code);

        Identity identity(String code);
    }
}
