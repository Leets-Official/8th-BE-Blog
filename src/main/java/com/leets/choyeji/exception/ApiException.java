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

    public static ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "로그인이 필요한 서비스입니다.");
    }

    public static ApiException loginFailed() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    public static ApiException duplicateEmail() {
        return new ApiException(HttpStatus.CONFLICT, "AUTH_409_1", "이미 가입된 이메일입니다.");
    }

    public static ApiException passwordTooLong() {
        return new ApiException(HttpStatus.BAD_REQUEST, "AUTH_400_1", "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
    }

    public static ApiException postNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "POST_404_1", "존재하지 않는 게시글입니다.");
    }

    public static ApiException forbidden() {
        return new ApiException(HttpStatus.FORBIDDEN, "POST_403_1", "게시글 접근 권한이 없습니다.");
    }
}
