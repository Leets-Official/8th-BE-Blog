package com.leets_8th_be.kanghyeonwoo.dto;

import java.time.LocalDateTime;

public record CommentResponse(Long id, Long authorId, Long parentId, String content,
                              CommentStatus status, boolean deleted, LocalDateTime createdAt) {
}
