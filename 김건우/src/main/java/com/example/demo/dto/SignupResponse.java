package com.example.demo.dto;

import com.example.demo.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답")
public record SignupResponse(
        @Schema(description = "회원 ID", example = "1") Long userId,
        @Schema(description = "이메일", example = "tester@test.com") String email,
        @Schema(description = "닉네임", example = "tester") String nickname
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
