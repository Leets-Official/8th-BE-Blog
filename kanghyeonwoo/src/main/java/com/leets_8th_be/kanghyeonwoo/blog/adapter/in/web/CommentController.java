package com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web;

import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.INVALID_INPUT_VALUE;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.POST_NOT_FOUND;
import static com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.TYPE_MISMATCH;

import com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web.dto.CommentResponse;
import com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web.dto.CommentStatus;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentPage;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CommentSearchQuery;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreateCommentCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreateCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentsUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.ManageOwnCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentState;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.shared.security.CurrentUser;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiPaths;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import com.leets_8th_be.kanghyeonwoo.shared.web.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 목록·본문 검색")
@RequestMapping(ApiPaths.API_V2)
public class CommentController {
    private final CurrentUser currentUser;
    private final CreateCommentUseCase createComment;
    private final ManageOwnCommentUseCase ownComment;

    public record CommentWrite(@NotBlank String content, @Positive Long parentId) {}

    @ApiErrorCodes({
        INVALID_INPUT_VALUE,
        POST_NOT_FOUND,
        ApiResponseCode.UNAUTHORIZED,
        ApiResponseCode.POST_ACCESS_DENIED,
        ApiResponseCode.COMMENT_NOT_FOUND
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "성공",
            useReturnTypeSchema = true)
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> create(
            @PathVariable Long postId, @Valid @RequestBody CommentWrite request) {
        CommentId parentId = request.parentId() == null ? null : new CommentId(request.parentId());
        CreateCommentCommand command =
                new CreateCommentCommand(
                        new PostId(postId), currentUser.requireId(), parentId, request.content());
        CommentView result = createComment.create(command);
        return ResponseEntity.status(201)
                .body(
                        ApiResponse.success(
                                ApiResponseCode.RESOURCE_CREATED,
                                "댓글이 등록되었습니다.",
                                toResponse(result)));
    }

    @ApiErrorCodes({
        INVALID_INPUT_VALUE,
        POST_NOT_FOUND,
        ApiResponseCode.UNAUTHORIZED,
        ApiResponseCode.POST_ACCESS_DENIED,
        ApiResponseCode.COMMENT_NOT_FOUND
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            useReturnTypeSchema = true)
    @PatchMapping("/comments/{commentId}")
    public ApiResponse<?> update(
            @PathVariable Long commentId, @Valid @RequestBody CommentWrite request) {
        return ApiResponse.success(
                toResponse(
                        ownComment.update(
                                new CommentId(commentId),
                                currentUser.requireId(),
                                request.content())));
    }

    @ApiErrorCodes({
        INVALID_INPUT_VALUE,
        POST_NOT_FOUND,
        ApiResponseCode.UNAUTHORIZED,
        ApiResponseCode.POST_ACCESS_DENIED,
        ApiResponseCode.COMMENT_NOT_FOUND
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            useReturnTypeSchema = true)
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<?> delete(@PathVariable Long commentId) {
        return ApiResponse.success(
                toResponse(
                        ownComment.deleteOwn(new CommentId(commentId), currentUser.requireId())));
    }

    private final GetCommentsUseCase getComments;

    @GetMapping("/posts/{postId}/comments")
    @Operation(
            summary = "댓글 목록·본문 검색",
            description = "대댓글은 parentId로 연결합니다. 숨김·삭제 댓글은 목록에서 안내 문구로 표시하고 검색에서는 제외합니다.")
    @ApiErrorCodes({INVALID_INPUT_VALUE, TYPE_MISMATCH, POST_NOT_FOUND})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "댓글 목록 조회 성공",
            useReturnTypeSchema = true)
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(
            @Parameter(description = "댓글을 조회할 게시글 ID", example = "1") @PathVariable("postId")
                    Long postId,
            @Parameter(description = "1부터 시작하는 페이지 번호", schema = @Schema(minimum = "1"))
                    @RequestParam(name = "page", defaultValue = "1")
                    int page,
            @Parameter(
                            description = "페이지 크기 (최대 100)",
                            schema = @Schema(minimum = "1", maximum = "100", example = "10"))
                    @RequestParam(name = "size", defaultValue = "10")
                    int size,
            @Parameter(description = "댓글 본문 검색어", example = "질문")
                    @RequestParam(name = "keyword", required = false)
                    String keyword) {
        CommentPage result =
                getComments.getComments(
                        new CommentSearchQuery(new PostId(postId), page, size, keyword));
        return ResponseEntity.ok(ApiResponse.success("댓글 조회가 완료되었습니다.", toResponse(result)));
    }

    private static PageResponse<CommentResponse> toResponse(CommentPage page) {
        return new PageResponse<>(
                page.items().stream().map(CommentController::toResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }

    private static CommentResponse toResponse(CommentView view) {
        CommentStatus status =
                view.state() == CommentState.ACTIVE ? CommentStatus.ACTIVE : CommentStatus.HIDDEN;
        return new CommentResponse(
                view.id().value(),
                view.authorId().value(),
                view.parentId() == null ? null : view.parentId().value(),
                view.displayContent(),
                status,
                view.deleted(),
                view.createdAt());
    }
}
