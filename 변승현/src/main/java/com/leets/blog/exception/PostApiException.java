package com.leets.blog.exception;

import lombok.Getter;

@Getter
public class PostApiException extends RuntimeException {
    private final int status;
    private final String code;

    public PostApiException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
