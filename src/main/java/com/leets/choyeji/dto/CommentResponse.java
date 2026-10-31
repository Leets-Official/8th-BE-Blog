package com.leets.choyeji.dto;

import com.leets.choyeji.entity.Comment;
import java.time.LocalDate;

public record CommentResponse(Long commentId, Long postId, String content, String writer, LocalDate createdDate) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getPost().getId(), comment.getContent(),
                comment.getMember().getNickname(), comment.getCreatedAt().toLocalDate());
    }
}
