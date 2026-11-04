package com.example.demo.global;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;

@Schema(description = "공통 응답 형식")
public record ApiResponse<T>(
        @Schema(description = "요청 성공 여부", example = "true")
        @JsonProperty("isSuccess") boolean isSuccess,
        @Schema(description = "응답 코드 (예: POST_201_1)", example = "POST_201_1")
        String code,
        @Schema(description = "응답 메시지", example = "게시글이 생성되었습니다.")
        String message,
        @Schema(description = "응답 데이터 (없으면 null)")
        T data
) {
    public static <T> ResponseEntity<ApiResponse<T>> success(SuccessCode code, T data) {
        return ResponseEntity.status(code.getStatus())
                .body(new ApiResponse<>(true, code.getCode(), code.getMessage(), data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> failure(ErrorCode code, T data) {
        return ResponseEntity.status(code.getStatus())
                .body(new ApiResponse<>(false, code.getCode(), code.getMessage(), data));
    }
}
