package com.leets.blog.domain.member.web.dto.response;

import com.leets.blog.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

// 회원가입 응답 (비밀번호는 포함하지 않음)
@Schema(description = "회원가입 응답")
public record MemberSignUpResponse(

        @Schema(description = "회원 ID (X-Member-Id 헤더에 사용)", example = "1")
        Long memberId,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "이메일", example = "hong@test.com")
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
