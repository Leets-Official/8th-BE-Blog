package com.leets.choyeji.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PostListResponse(List<PostResponse> posts, int page, int size,
                               long totalElements, int totalPages, boolean hasNext) {
    public static PostListResponse from(Page<PostResponse> result) {
        return new PostListResponse(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.hasNext());
    }
}
