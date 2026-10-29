package com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentPage;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CommentSearchQuery;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentsUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentState;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiPaths;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web.dto.CommentResponse;
import com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web.dto.CommentStatus;
import com.leets_8th_be.kanghyeonwoo.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 목록·본문 검색")
@RequestMapping(ApiPaths.API_V1)
public class CommentController {
    private final GetCommentsUseCase getComments;

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 목록·본문 검색", description = "대댓글은 parentId로 연결합니다. 숨김·삭제 댓글은 목록에서 안내 문구로 표시하고 검색에서는 제외합니다.")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(
            @PathVariable("postId") Long postId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword) {
        CommentPage result = getComments.getComments(new CommentSearchQuery(new PostId(postId), page, size, keyword));
        return ResponseEntity.ok(ApiResponse.success("댓글 조회가 완료되었습니다.", toResponse(result)));
    }

    private static PageResponse<CommentResponse> toResponse(CommentPage page) {
        return new PageResponse<>(page.items().stream().map(CommentController::toResponse).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages());
    }

    private static CommentResponse toResponse(CommentView view) {
        var comment = view.comment();
        CommentStatus status = comment.state() == CommentState.ACTIVE ? CommentStatus.ACTIVE : CommentStatus.HIDDEN;
        return new CommentResponse(comment.id().value(), comment.authorId().value(),
                comment.parentId() == null ? null : comment.parentId().value(), comment.displayContent(),
                status, comment.deleted(), view.createdAt());
    }
}
