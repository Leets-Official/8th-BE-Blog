package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 부분 수정(PATCH): title, content 는 null 허용 (null = 수정 안 함)
// (?s) 는 줄바꿈이 있는 본문도 통과시키기 위한 옵션
@Schema(description = "게시글 부분 수정 요청. title, content 중 보낸 값만 수정되며 둘 다 없으면 400")
public record PostPatchRequest(
        @Schema(description = "요청자 ID. 글 작성자와 같아야 함", example = "1")
        @NotNull(message = "userId를 입력해주세요.") Long userId,
        @Schema(description = "수정할 제목 (생략하면 유지, 최대 255자)", example = "수정한 제목", nullable = true)
        @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
        @Pattern(regexp = "(?s).*\\S.*", message = "제목은 공백일 수 없습니다.") String title,
        @Schema(description = "수정할 내용 (생략하면 유지)", example = "수정한 내용", nullable = true)
        @Pattern(regexp = "(?s).*\\S.*", message = "내용은 공백일 수 없습니다.") String content
) {}
