package com.leets.choyeji.config;

import com.leets.choyeji.dto.ApiResponse;
import com.leets.choyeji.exception.ApiException;
import java.util.Map;

public enum SwaggerError {
    INVALID_REQUEST("JSON 형식 오류, 요청 본문 누락 또는 경로·쿼리 파라미터 형식 오류", (Map<String, String>) null),
    SIGNUP_INPUT("이메일 형식·필수값, 비밀번호 8~50자 또는 닉네임 최대 50자 검증 실패",
            Map.of("email", "올바른 이메일 형식으로 입력해주세요.")),
    LOGIN_INPUT("이메일 형식·필수값 또는 비밀번호 8~50자 검증 실패",
            Map.of("email", "올바른 이메일 형식으로 입력해주세요.")),
    POST_INPUT("제목·내용 필수값 또는 제목 최대 255자 검증 실패",
            Map.of("title", "제목을 입력해주세요.")),
    COMMENT_INPUT("댓글 내용이 공백이거나 최대 10000자 초과",
            Map.of("content", "댓글 내용을 입력해주세요.")),
    REPORT_INPUT("신고 사유가 공백이거나 최대 500자 초과",
            Map.of("reason", "신고 사유를 입력해주세요.")),
    RESOLUTION_INPUT("처리 결과 누락. ACCEPTED 또는 REJECTED가 필요합니다.",
            Map.of("result", "처리 결과를 입력해주세요. ACCEPTED 또는 REJECTED를 사용해주세요.")),
    PASSWORD_TOO_LONG("비밀번호가 UTF-8 기준 72바이트 초과", ApiException.passwordTooLong()),
    UNAUTHORIZED("토큰 누락·만료·위조 또는 토큰에 해당하는 회원이 없음", ApiException.unauthorized()),
    LOGIN_FAILED("가입하지 않은 이메일 또는 비밀번호 불일치", ApiException.loginFailed()),
    DUPLICATE_EMAIL("이미 가입된 이메일. 공백 제거·소문자 변환 후 비교합니다.", ApiException.duplicateEmail()),
    POST_FORBIDDEN("로그인한 회원이 게시글 작성자가 아님", ApiException.forbidden()),
    ADMIN_REQUIRED("로그인한 회원의 DB 역할이 ADMIN이 아님", ApiException.adminRequired()),
    POST_NOT_FOUND("게시글이 존재하지 않거나 HIDDEN 상태", ApiException.postNotFound()),
    RESTORE_POST_NOT_FOUND("숨김 해제 대상 게시글이 존재하지 않음", ApiException.postNotFound()),
    REPORT_NOT_FOUND("신고가 존재하지 않음", ApiException.reportNotFound()),
    DUPLICATE_REPORT("동일 회원이 동일 게시글을 이미 신고함. 동시 중복 요청도 차단합니다.", ApiException.duplicateReport()),
    REPORT_ALREADY_RESOLVED("이미 RESOLVED인 신고. 동일 결과로 재요청해도 처리할 수 없습니다.", ApiException.reportAlreadyResolved());

    private final String description;
    private final int status;
    private final ApiResponse<?> example;

    SwaggerError(String description, Map<String, String> fields) {
        this.description = description;
        this.status = 400;
        this.example = ApiResponse.failure("COMMON_400_1", "올바르지 않은 입력값입니다.", fields);
    }

    SwaggerError(String description, ApiException exception) {
        this.description = description;
        this.status = exception.getStatus().value();
        this.example = ApiResponse.failure(exception.getCode(), exception.getMessage(), null);
    }

    public String responseCode() {
        return String.valueOf(status);
    }

    public String description() {
        return example.code() + ": " + description;
    }

    public ApiResponse<?> example() {
        return example;
    }
}
