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

    // X-Member-Id로 받은 회원 확인, 없거나 탈퇴한 회원이면 401
    public Member getActiveMember(Long memberId) {
        if (memberId < 1) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
    }

    // 관리자 전용 기능: 회원 확인(401) 후 관리자가 아니면 403
    public Member getAdmin(Long memberId) {
        Member member = getActiveMember(memberId);
        if (!member.isAdmin()) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        return member;
    }
}
