package com.leets_8th_be.kanghyeonwoo.blog.application.exception;

public class PostNotFound extends RuntimeException {
    public PostNotFound() {
        super("게시글을 찾을 수 없습니다.");
    }
}
