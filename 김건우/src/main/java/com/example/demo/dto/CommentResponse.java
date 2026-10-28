package com.example.demo.dto;

import com.example.demo.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        Long postId,
        String writer,
        String content,
        boolean accepted,
        LocalDateTime createdAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getPost().getId(),
                comment.getUser().getNickname(), comment.getContent(),
                comment.isAccepted(), comment.getCreatedAt());
    }
}
