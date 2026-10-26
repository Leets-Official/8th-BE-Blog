package com.leets.mission.global.apiPayload.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 성공 응답 코드,메시지
@Getter
@RequiredArgsConstructor
public enum SuccessStatus {

    OK(HttpStatus.OK, "2000", "OK"),

    // 게시글
    POST_CREATED(HttpStatus.CREATED, "2000", "게시글이 생성되었습니다."),
    POST_UPDATED(HttpStatus.OK, "2000", "게시글이 수정되었습니다."),
    POST_DELETED(HttpStatus.OK, "2000", "게시글이 삭제되었습니다."),

    // 댓글
    COMMENT_CREATED(HttpStatus.CREATED, "2000", "댓글이 작성되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
