package com.leets.blog.domain.member.repository;

import com.leets.blog.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);
    // 로그인 / 회원 조회
    Optional<Member> findByEmailAndDeletedAtIsNull(String email);
    // 회원 가입시 이메일 중복 체크
    boolean existsByEmail(String email);
}
