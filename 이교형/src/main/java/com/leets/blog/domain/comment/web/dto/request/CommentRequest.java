package com.leets.blog.domain.comment.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 댓글 작성·수정 공통 요청 (필드가 content 하나라 검증 규칙이 같음)
// 검증 message에는 응답할 ErrorCode 이름을 적음
@Schema(description = "댓글 작성·수정 요청")
public record CommentRequest(

        @Schema(description = "댓글 내용 (공백 불가, 1,000자 이하)", example = "좋은 글이네요.")
        @NotBlank(message = "REQUIRED_FIELD_MISSING")
        @Size(max = 1_000, message = "COMMENT_LENGTH_EXCEED")
        String content
) {
}
