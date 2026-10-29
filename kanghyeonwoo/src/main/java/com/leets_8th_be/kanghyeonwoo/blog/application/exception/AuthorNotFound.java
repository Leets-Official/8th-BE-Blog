package com.leets_8th_be.kanghyeonwoo.blog.application.exception;

public class AuthorNotFound extends RuntimeException {
    public AuthorNotFound() {
        super("게시글 작성자 프로필을 찾을 수 없습니다.");
    }
}
