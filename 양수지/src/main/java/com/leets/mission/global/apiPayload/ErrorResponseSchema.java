package com.leets.mission.global.apiPayload;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "공통 에러 응답")
public record ErrorResponseSchema(
        @Schema(example = "false") Boolean isSuccess,
        @Schema(example = "POST_404_1") String code,
        @Schema(example = "게시글을 찾을 수 없습니다.") String message,
        @Schema(description = "검증 실패 시 필드별 오류", nullable = true) Map<String, String> result
) {
}