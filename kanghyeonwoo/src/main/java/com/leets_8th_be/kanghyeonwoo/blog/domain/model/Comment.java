package com.leets_8th_be.kanghyeonwoo.blog.domain.model;

import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;

public record Comment(CommentId id, UserId authorId, CommentId parentId, String content,
                      CommentState state, boolean deleted) {
    public static Comment create(UserId authorId, CommentId parentId, String content) {
        return new Comment(null, authorId, parentId, content, CommentState.ACTIVE, false);
    }

    public String displayContent() {
        if (deleted) return "삭제된 댓글입니다.";
        if (state == CommentState.HIDDEN) return "숨김 처리된 댓글입니다.";
        return content;
    }

    public Comment hide() {
        if (state == CommentState.HIDDEN) return this;
        return new Comment(id, authorId, parentId, content, CommentState.HIDDEN, deleted);
    }

    public Comment delete() {
        if (deleted) return this;
        return new Comment(id, authorId, parentId, content, state, true);
    }
}
