package com.leets.mission.dto.comment;

import com.leets.mission.domain.comment.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long postId,
        String content,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getMember().getNickname(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}