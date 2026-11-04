package com.example.demo.global;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Swagger 문서 전용 에러 응답 스키마.
 * 실제 에러 응답은 {@link ApiResponse#failure} 가 만들며, 그 모양을 문서에 보여주기 위한 클래스다.
 */
@Schema(description = "공통 에러 응답 형식")
public record ErrorResponse(
        @Schema(description = "항상 false", example = "false")
        @JsonProperty("isSuccess") boolean isSuccess,
        @Schema(description = "에러 코드 (도메인_HTTP상태_번호)", example = "POST_404_1")
        String code,
        @Schema(description = "에러 메시지", example = "존재하지 않는 게시글입니다.")
        String message,
        @Schema(description = "추가 정보. 입력값 검증 실패 시 { 필드명: 사유 }, 그 외에는 null", nullable = true)
        Object data
) {}
