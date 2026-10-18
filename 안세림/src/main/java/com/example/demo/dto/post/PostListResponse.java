package com.example.demo.dto.post;

import java.time.LocalDateTime;

public record PostListResponse(Long id, String title, Long authorId, LocalDateTime createdAt) {
}