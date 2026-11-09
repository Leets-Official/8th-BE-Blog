package com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web;

import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.INVALID_INPUT_VALUE;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.POST_NOT_FOUND;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.TYPE_MISMATCH;

import com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes;
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
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping(ApiPaths.API_V2)
public class CommentController {
    private final com.leets_8th_be.kanghyeonwoo.shared.security.CurrentUser currentUser;
    private final com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreateCommentUseCase createComment;
    private final com.leets_8th_be.kanghyeonwoo.blog.application.port.in.ManageOwnCommentUseCase ownComment;
    public record CommentWrite(@jakarta.validation.constraints.NotBlank String content, @jakarta.validation.constraints.Positive Long parentId) {}
    @ApiErrorCodes({INVALID_INPUT_VALUE, POST_NOT_FOUND, com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.UNAUTHORIZED, com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.POST_ACCESS_DENIED, com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.COMMENT_NOT_FOUND})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201",description="성공",useReturnTypeSchema=true)
    @org.springframework.web.bind.annotation.PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> create(@PathVariable Long postId,@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody CommentWrite r) {
        var result=createComment.create(new com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreateCommentCommand(new PostId(postId),currentUser.requireId(),r.parentId()==null?null:new com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId(r.parentId()),r.content()));
        return ResponseEntity.status(201).body(ApiResponse.success(com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.RESOURCE_CREATED,"댓글이 등록되었습니다.",toResponse(result)));
    }
    @ApiErrorCodes({INVALID_INPUT_VALUE, POST_NOT_FOUND, com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.UNAUTHORIZED, com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.POST_ACCESS_DENIED, com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.COMMENT_NOT_FOUND})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="성공",useReturnTypeSchema=true)
    @org.springframework.web.bind.annotation.PatchMapping("/comments/{commentId}")
    public ApiResponse<?> update(@PathVariable Long commentId,@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody CommentWrite r) {
        return ApiResponse.success(toResponse(ownComment.update(new com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId(commentId),currentUser.requireId(),r.content())));
    }
    @ApiErrorCodes({INVALID_INPUT_VALUE, POST_NOT_FOUND, com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.UNAUTHORIZED, com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.POST_ACCESS_DENIED, com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.COMMENT_NOT_FOUND})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="성공",useReturnTypeSchema=true)
    @org.springframework.web.bind.annotation.DeleteMapping("/comments/{commentId}")
    public ApiResponse<?> delete(@PathVariable Long commentId) {
        return ApiResponse.success(toResponse(ownComment.deleteOwn(new com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId(commentId),currentUser.requireId())));
    }
    private final GetCommentsUseCase getComments;

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 목록·본문 검색", description = "대댓글은 parentId로 연결합니다. 숨김·삭제 댓글은 목록에서 안내 문구로 표시하고 검색에서는 제외합니다.")
    @ApiErrorCodes({INVALID_INPUT_VALUE, TYPE_MISMATCH, POST_NOT_FOUND})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공", useReturnTypeSchema = true)
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(
            @Parameter(description = "댓글을 조회할 게시글 ID", example = "1") @PathVariable("postId") Long postId,
            @Parameter(description = "1부터 시작하는 페이지 번호", schema = @io.swagger.v3.oas.annotations.media.Schema(minimum = "1"))
            @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "페이지 크기 (최대 100)", schema = @io.swagger.v3.oas.annotations.media.Schema(minimum = "1", maximum = "100", example = "10"))
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "댓글 본문 검색어", example = "질문")
            @RequestParam(name = "keyword", required = false) String keyword) {
        CommentPage result = getComments.getComments(new CommentSearchQuery(new PostId(postId), page, size, keyword));
        return ResponseEntity.ok(ApiResponse.success("댓글 조회가 완료되었습니다.", toResponse(result)));
    }

    private static PageResponse<CommentResponse> toResponse(CommentPage page) {
        return new PageResponse<>(page.items().stream().map(CommentController::toResponse).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages());
    }

    private static CommentResponse toResponse(CommentView view) {
        CommentStatus status = view.state() == CommentState.ACTIVE ? CommentStatus.ACTIVE : CommentStatus.HIDDEN;
        return new CommentResponse(view.id().value(), view.authorId().value(),
                view.parentId() == null ? null : view.parentId().value(), view.displayContent(),
                status, view.deleted(), view.createdAt());
    }
}
