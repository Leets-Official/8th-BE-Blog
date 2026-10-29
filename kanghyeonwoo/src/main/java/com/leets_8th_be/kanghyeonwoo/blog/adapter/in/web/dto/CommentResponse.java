package com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web.dto;

import java.time.LocalDateTime;

public record CommentResponse(Long id, Long authorId, Long parentId, String content,
                              CommentStatus status, boolean deleted, LocalDateTime createdAt) {
}
