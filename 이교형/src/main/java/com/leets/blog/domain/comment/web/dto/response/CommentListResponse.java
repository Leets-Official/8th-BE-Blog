package com.leets.blog.domain.comment.web.dto.response;

import com.leets.blog.domain.comment.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "댓글 목록 응답")
public record CommentListResponse(

        @Schema(description = "댓글 목록 (오래된 순)")
        List<CommentResponse> comments,

        @Schema(description = "현재 페이지 (1부터 시작)", example = "1")
        int page,

        @Schema(description = "페이지 크기", example = "10")
        int size,

        @Schema(description = "전체 댓글 수 (삭제 제외)", example = "25")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
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
