package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignupResponse(
        @Schema(description = "생성된 회원 ID", example = "1") Long memberId,
        @Schema(description = "저장된 이메일", example = "leets@example.com") String email,
        @Schema(description = "저장된 닉네임", example = "리츠회원") String nickname
) {}
