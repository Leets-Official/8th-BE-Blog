package com.leets.mission.global.apiPayload.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 실패 응답 코드,a메시지 ({도메인}_{HTTP 상태}_{번호} 형식)
@Getter
@RequiredArgsConstructor
public enum ErrorStatus {

    // 공통
    COMMON_400_1(HttpStatus.BAD_REQUEST, "COMMON_400_1", "입력값이 올바르지 않습니다."),
    COMMON_400_2(HttpStatus.BAD_REQUEST, "COMMON_400_2", "사용자 정보(userId)가 필요합니다."),
    COMMON_500_1(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_1", "서버 오류가 발생했습니다."),

    // 사용자
    USER_404_1(HttpStatus.NOT_FOUND, "USER_404_1", "사용자를 찾을 수 없습니다."),

    // 게시글
    POST_400_1(HttpStatus.BAD_REQUEST, "POST_400_1", "수정할 제목 또는 본문을 입력해주세요."),
    POST_400_2(HttpStatus.BAD_REQUEST, "POST_400_2", "page는 0 이상, size는 1 이상이어야 합니다."),
    POST_403_1(HttpStatus.FORBIDDEN, "POST_403_1", "본인이 작성한 게시글만 수정할 수 있습니다."),
    POST_403_2(HttpStatus.FORBIDDEN, "POST_403_2", "본인이 작성한 게시글만 삭제할 수 있습니다."),
    POST_403_3(HttpStatus.FORBIDDEN, "POST_403_3", "본인이 작성한 게시글만 숨길 수 있습니다."),
    POST_404_1(HttpStatus.NOT_FOUND, "POST_404_1", "게시글을 찾을 수 없습니다."),
    POST_409_1(HttpStatus.CONFLICT, "POST_409_1", "이미 숨김 처리된 게시글입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
