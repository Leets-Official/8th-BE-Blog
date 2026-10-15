package com.example.demo.dto;

public record PostResponse(
        Long id,
        String title,
        String content,
        boolean isPrivate,
        Long userId
) {
}
