package com.leets.blog.common.swagger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.leets.blog.common.response.ErrorBody;
import io.swagger.v3.oas.annotations.media.Schema;

// Swagger 문서 전용: 실패 응답 형태 (실제 응답은 ApiResponse.fail()이 만듦, 필드 구성을 ApiResponse와 같게 유지)
@Schema(name = "ErrorResponse", description = "공통 실패 응답")
public record ErrorResponse(

        @JsonProperty("isSuccess")
        @Schema(description = "성공 여부 (실패 시 항상 false)", example = "false")
        boolean isSuccess,

        @Schema(description = "HTTP 상태 코드", example = "404")
        String code,

        @Schema(description = "실패 시 항상 null", nullable = true)
        Object data,

        @Schema(description = "에러 정보")
        ErrorBody error
) {
}
