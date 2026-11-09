package com.example.demo.auth.repository;

import com.example.demo.auth.entity.RefreshToken;
import com.example.demo.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);          // 로그아웃
    void deleteAllByMember(Member member);     // 모든 기기 로그아웃
}