package com.leets_8th_be.kanghyeonwoo.blog.domain.model;

import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;

public record Post(PostId id, String title, String content, UserId authorId, PostState state) {
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
        if (state == null) {
            throw new IllegalArgumentException("게시글 상태는 필수입니다.");
        }
    }

    public static Post create(String title, String content, UserId authorId) {
        return new Post(null, title, content, authorId, PostState.ACTIVE);
    }

    public void requireAuthor(UserId actorId) {
        if (!authorId.equals(actorId)) throw new PostAccessDenied();
    }

    public Post update(UserId actorId, String title, String content) {
        requireAuthor(actorId);
        return new Post(id, title, content, authorId, state);
    }

    public Post hide() {
        if (state == PostState.HIDDEN) return this;
        return new Post(id, title, content, authorId, PostState.HIDDEN);
    }
}
