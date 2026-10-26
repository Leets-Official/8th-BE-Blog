package com.leets_8th_be.kanghyeonwoo.dto;

import com.leets_8th_be.kanghyeonwoo.entity.Comment;
import com.leets_8th_be.kanghyeonwoo.entity.CommentStatus;
import java.time.LocalDateTime;

public record CommentResponse(Long id, Long authorId, Long parentId, String content,
                              CommentStatus status, boolean deleted, LocalDateTime createdAt) {
    public static CommentResponse from(Comment comment) {
        String content = comment.isDeleted() ? "삭제된 댓글입니다."
                : comment.getStatus() == CommentStatus.HIDDEN ? "숨김 처리된 댓글입니다." : comment.getContent();
        return new CommentResponse(comment.getId(), comment.getUser().getId(),
                comment.getParent() == null ? null : comment.getParent().getId(),
                content, comment.getStatus(), comment.isDeleted(), comment.getCreatedAt());
    }
}
