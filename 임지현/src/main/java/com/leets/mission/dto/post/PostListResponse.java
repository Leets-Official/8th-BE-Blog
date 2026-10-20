package com.leets.mission.dto.post;

import org.springframework.data.domain.Page;
import java.util.List;

public record PostListResponse(
        List<PostResponse> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static PostListResponse of(Page<PostResponse> page) {
        return new PostListResponse(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}