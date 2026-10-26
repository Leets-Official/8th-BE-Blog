package com.leets.mission.global.apiPayload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.leets.mission.global.apiPayload.code.ErrorStatus;
import com.leets.mission.global.apiPayload.code.SuccessStatus;

// 모든 API 응답을 { isSuccess, code, message, result } 형식으로 통일흔 ㄴ 공통 응답
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public record ApiResponse<T>(
        @JsonProperty("isSuccess") Boolean isSuccess,
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL) T result // result가 null이면 JSON에서 생략
) {

    // 성공 응답 (200 OK)
    public static <T> ApiResponse<T> onSuccess(T result) {
        return of(SuccessStatus.OK, result);
    }

    // 상황별 성공 메시지 (생성, 수정, 삭제)
    public static <T> ApiResponse<T> of(SuccessStatus status, T result) {
        return new ApiResponse<>(true, status.getCode(), status.getMessage(), result);
    }

    // 실패 응답 (result에 필드별 검증 오류 등을 담을 수 있음)
    public static <T> ApiResponse<T> onFailure(ErrorStatus status, T result) {
        return new ApiResponse<>(false, status.getCode(), status.getMessage(), result);
    }
}
