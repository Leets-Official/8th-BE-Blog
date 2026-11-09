package com.leets.blog.domain.auth.web.dto.response;

import com.leets.blog.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

// 회원가입 응답 (비밀번호는 포함하지 않음)
@Schema(description = "회원가입 응답")
public record SignUpResponse(

        @Schema(description = "회원 ID", example = "1")
        Long memberId,

        @Schema(description = "이메일", example = "hong@test.com")
        String email,

        @Schema(description = "닉네임", example = "길동이")
        String nickname
) {

    public static SignUpResponse from(Member member) {
        return new SignUpResponse(member.getId(), member.getEmail(), member.getNickname());
    }
}
