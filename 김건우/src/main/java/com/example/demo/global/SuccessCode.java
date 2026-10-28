package com.example.demo.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
    POST_LIST_OK(HttpStatus.OK, "POST_200_1", "게시글 목록 조회에 성공했습니다."),
    POST_DETAIL_OK(HttpStatus.OK, "POST_200_2", "게시글 조회에 성공했습니다."),
    POST_UPDATED(HttpStatus.OK, "POST_200_3", "게시글이 수정되었습니다."),
    POST_DELETED(HttpStatus.OK, "POST_200_4", "게시글이 삭제되었습니다."),
    POST_CREATED(HttpStatus.CREATED, "POST_201_1", "게시글이 생성되었습니다."),

    // 4주차
    POST_HIDDEN(HttpStatus.OK, "POST_200_5", "게시글이 숨김 처리되었습니다."),
    REPORT_CREATED(HttpStatus.CREATED, "REPORT_201_1", "게시글 신고가 접수되었습니다."),
    REPORT_RESOLVED(HttpStatus.OK, "REPORT_200_1", "신고가 처리 완료되었습니다."),
    COMMENT_CREATED(HttpStatus.CREATED, "COMMENT_201_1", "댓글이 생성되었습니다."),
    COMMENT_ACCEPTED(HttpStatus.OK, "COMMENT_200_1", "댓글이 채택되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
