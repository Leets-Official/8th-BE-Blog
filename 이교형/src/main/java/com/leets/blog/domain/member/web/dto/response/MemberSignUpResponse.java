package com.leets.blog.domain.member.web.dto.response;

import com.leets.blog.domain.member.entity.Member;

// 회원가입 응답 (비밀번호는 포함하지 않음)
public record MemberSignUpResponse(
        Long memberId,
        String name,
        String email
) {

    public static MemberSignUpResponse from(Member member) {
        return new MemberSignUpResponse(
                member.getId(),
                member.getName(),
                member.getEmail()
        );
    }
}
