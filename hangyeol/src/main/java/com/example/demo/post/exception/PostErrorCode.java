package com.example.demo.post.exception;

import com.example.demo.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PostErrorCode implements BaseErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST,
            "COMMON400_1",
            "제목과 내용은 필수 입력 항목입니다."),
    TITLE_TOO_LONG(HttpStatus.BAD_REQUEST,
            "POST400_1",
            "게시글 제목은 50자를 초과할 수 없습니다."),
    INVALID_REQUEST_FORMAT(HttpStatus.BAD_REQUEST,
            "COMMON400_2",
            "요청 데이터 형식이 올바르지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "MEMBER404_1",
            "존재하지 않는 사용자입니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND,
            "POST400_2,",
            "존재하지 않는 게시물입니다."),
    POST_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "POST403_1",
            "게시글을 수정/삭제할 권한이 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
