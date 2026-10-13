package com.leets.blog.domain.post.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 수정 요청 (바꿀 필드만 전송)")
public record PostUpdateRequest(

        @Schema(description = "제목 (보낸 경우 공백 불가, 100자 이하)", example = "수정한 제목")
        @Pattern(regexp = "(?s).*\\S.*", message = "REQUIRED_FIELD_MISSING")
        @Size(max = 100, message = "TITLE_LENGTH_EXCEED")
        String title,

        @Schema(description = "본문 (보낸 경우 공백 불가, 100,000자 이하)", example = "수정한 내용")
        @Pattern(regexp = "(?s).*\\S.*", message = "REQUIRED_FIELD_MISSING")
        @Size(max = 100_000, message = "CONTENT_LENGTH_EXCEED")
        String content
) {

    // 아무 필드도 보내지 않은 요청은 400
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "REQUIRED_FIELD_MISSING")
    public boolean isAnyFieldPresent() {
        return title != null || content != null;
    }
}
