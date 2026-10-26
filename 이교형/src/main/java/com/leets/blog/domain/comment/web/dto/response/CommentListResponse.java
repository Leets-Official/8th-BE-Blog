package com.leets.blog.domain.comment.web.dto.response;

import com.leets.blog.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;

import java.util.List;

public record CommentListResponse(
        List<CommentResponse> comments,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static CommentListResponse from(Page<Comment> result) {
        return new CommentListResponse(
                result.getContent().stream().map(CommentResponse::from).toList(),
                result.getNumber() + 1,     // Spring은 0부터, 명세는 1부터
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }
}
