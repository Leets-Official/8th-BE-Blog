package com.example.demo.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    Optional<Member> findByEmailAndDeletedAtIsNull(String email);

    // 동시에 재발급하더라도 같은 Refresh Token은 한 번만 사용합니다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Member> findByRefreshTokenHashAndDeletedAtIsNull(String refreshTokenHash);
}
