package org.example._thbe.domain.post.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example._thbe.global.code.BaseCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PostErrorCode implements BaseCode {

    NOT_MY_POST(HttpStatus.FORBIDDEN,"POST_403_1","게시글 접근 권한이 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND,"POST_404_1","존재하지 않는 게시물입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

}
