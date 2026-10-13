package com.leets.blog.domain.post.web.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PostListResponse(
        List<PostSummaryResponse> posts,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static PostListResponse from(Page<PostSummaryResponse> result) {
        return new PostListResponse(
                result.getContent(),
                result.getNumber() + 1,     // Spring은 0부터, 명세는 1부터
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }
}
