package com.leets.blog.domain.member.service;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.member.entity.Member;
import com.leets.blog.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 여러 서비스가 함께 쓰는 회원 조회 (호출한 서비스의 트랜잭션 안에서 실행)
@Component
@RequiredArgsConstructor
public class MemberReader {

    private final MemberRepository memberRepository;

    // 토큰의 회원 ID로 회원 확인, 토큰 발급 후 탈퇴한 회원이면 401
    public Member getActiveMember(Long memberId) {
        return memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
    }
}
