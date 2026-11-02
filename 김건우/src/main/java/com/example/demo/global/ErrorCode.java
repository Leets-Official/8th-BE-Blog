package com.example.demo.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_400_1", "올바르지 않은 입력값입니다."),
    NO_FIELD_TO_UPDATE(HttpStatus.BAD_REQUEST, "COMMON_400_2", "수정할 제목 또는 내용을 입력해주세요."),
    FORBIDDEN_POST(HttpStatus.FORBIDDEN, "COMMON_403_1", "게시글에 대한 권한이 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_404_1", "존재하지 않는 게시글입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_1", "존재하지 않는 사용자입니다."),

    // 4주차: 게시글 숨김
    POST_ALREADY_HIDDEN(HttpStatus.CONFLICT, "POST_409_1", "이미 숨김 처리된 게시글입니다."),

    // 4주차: 신고
    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "REPORT_400_1", "본인 게시글은 신고할 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_404_1", "존재하지 않는 신고입니다."),
    ALREADY_REPORTED(HttpStatus.CONFLICT, "REPORT_409_1", "이미 신고한 게시글입니다."),
    REPORT_ALREADY_RESOLVED(HttpStatus.CONFLICT, "REPORT_409_2", "이미 처리 완료된 신고입니다."),

    // 4주차: 댓글 채택
    SELF_ACCEPT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COMMENT_400_1", "본인 댓글은 채택할 수 없습니다."),
    NOT_POST_OWNER(HttpStatus.FORBIDDEN, "COMMENT_403_1", "게시글 작성자만 댓글을 채택할 수 있습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_404_1", "존재하지 않는 댓글입니다."),
    COMMENT_ALREADY_ACCEPTED(HttpStatus.CONFLICT, "COMMENT_409_1", "이미 채택된 댓글입니다."),
    ANOTHER_COMMENT_ACCEPTED(HttpStatus.CONFLICT, "COMMENT_409_2", "이미 채택된 댓글이 있는 게시글입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
