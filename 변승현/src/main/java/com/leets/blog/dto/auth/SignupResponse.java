package com.leets.blog.dto.auth;

import com.leets.blog.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record SignupResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "user@example.com") String email,
        @Schema(example = "승현") String nickname
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
