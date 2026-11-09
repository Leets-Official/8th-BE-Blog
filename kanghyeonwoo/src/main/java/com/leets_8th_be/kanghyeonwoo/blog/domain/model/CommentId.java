package com.leets_8th_be.kanghyeonwoo.blog.domain.model;

public record CommentId(Long value) {
    public CommentId {
        if (value == null) throw new IllegalArgumentException("댓글 ID는 필수입니다.");
    }
}
