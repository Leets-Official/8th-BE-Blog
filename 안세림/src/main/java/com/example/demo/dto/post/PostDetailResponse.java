package com.example.demo.dto.post;

import java.time.LocalDateTime;

public record PostDetailResponse(Long id, String title, String content, Long authorId,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
}