package com.leets.blog.domain.post.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 검증 message에는 응답할 ErrorCode 이름을 적음 (GlobalExceptionHandler가 변환)
@Schema(description = "게시글 생성 요청")
public record PostCreateRequest(

        @Schema(description = "제목 (공백 불가, 100자 이하)", example = "새 게시글")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        @Size(max = 100, message = "TITLE_LENGTH_EXCEED")
        String title,

        @Schema(description = "본문 (공백 불가, 100,000자 이하)", example = "내용입니다")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        @Size(max = 100_000, message = "CONTENT_LENGTH_EXCEED")
        String content
) {
}
