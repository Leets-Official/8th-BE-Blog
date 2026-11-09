package com.leets.blog.dto.auth;

import com.leets.blog.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 결과")
public record SignupResponse(
        @Schema(description = "회원 ID", example = "1")
        Long memberId,

        @Schema(description = "이메일", example = "newbie@leets.com")
        String email,

        @Schema(description = "닉네임", example = "새내기")
        String nickname
) {

    public static SignupResponse from(Member member) {
        return new SignupResponse(member.getId(), member.getEmail(), member.getNickname());
    }
}
