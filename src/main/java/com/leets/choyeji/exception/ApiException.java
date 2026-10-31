package com.leets.choyeji.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    /* 로그인 인증 복원 시 사용합니다.
    public static ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "로그인이 필요한 서비스입니다.");
    }
    */

    public static ApiException postNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "POST_404_1", "존재하지 않는 게시글입니다.");
    }

    /* 작성자 권한 검사 복원 시 사용합니다.
    public static ApiException forbidden() {
        return new ApiException(HttpStatus.FORBIDDEN, "POST_403_1", "게시글 접근 권한이 없습니다.");
    }
    */
}
