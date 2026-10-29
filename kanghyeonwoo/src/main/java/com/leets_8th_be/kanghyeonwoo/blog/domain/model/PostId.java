package com.leets_8th_be.kanghyeonwoo.blog.domain.model;

public record PostId(Long value) {
    public PostId {
        if (value == null) {
            throw new IllegalArgumentException("게시글 ID는 필수입니다.");
        }
    }
}
