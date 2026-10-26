package com.leets_8th_be.kanghyeonwoo.blog.domain.model;

import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;

public record Post(String title, String content, UserId authorId) {
    public Post {
        if (title == null || title.isBlank() || title.length() > 150) {
            throw new IllegalArgumentException("게시글 제목은 필수이며 150자 이하여야 합니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("게시글 본문은 필수입니다.");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("작성자 ID는 필수입니다.");
        }
    }
}
