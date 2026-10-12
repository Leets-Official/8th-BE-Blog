package com.leets.blog.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    POST_LIST_FOUND(HttpStatus.OK, "S200", "게시글 목록 조회가 완료되었습니다."),
    POST_FOUND(HttpStatus.OK, "S200", "게시글 조회가 완료되었습니다."),
    POST_CREATED(HttpStatus.CREATED, "S201", "게시글이 성공적으로 등록되었습니다."),
    POST_UPDATED(HttpStatus.OK, "S200", "게시글이 성공적으로 수정되었습니다."),
    POST_DELETED(HttpStatus.OK, "S200", "게시글이 성공적으로 삭제되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
