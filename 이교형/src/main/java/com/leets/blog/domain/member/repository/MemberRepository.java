package com.leets.blog.domain.member.repository;

import com.leets.blog.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);
    // 로그인 시 이메일로 회원 조회
    Optional<Member> findByEmailAndDeletedAtIsNull(String email);
    // 회원가입 시 이메일 중복 체크 (탈퇴 회원 포함: unique 제약 때문에 재사용 불가)
    boolean existsByEmail(String email);
    // 회원가입 시 닉네임 중복 체크 (탈퇴 회원 포함)
    boolean existsByNickname(String nickname);
}
