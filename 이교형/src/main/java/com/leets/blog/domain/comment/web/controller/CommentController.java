package com.leets.blog.domain.comment.web.controller;

import com.leets.blog.common.exception.BindErrorCode;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.common.response.ApiResponse;
import com.leets.blog.domain.comment.service.CommentService;
import com.leets.blog.domain.comment.web.dto.request.CommentRequest;
import com.leets.blog.domain.comment.web.dto.response.CommentDeleteResponse;
import com.leets.blog.domain.comment.web.dto.response.CommentListResponse;
import com.leets.blog.domain.comment.web.dto.response.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Comment API", description = "댓글 CRUD API")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다. 삭제·숨김된 글에는 작성할 수 없습니다.")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Parameter(description = "작성자 회원 ID (로그인 대체)", required = true)
            @RequestHeader("X-Member-Id") @BindErrorCode(ErrorCode.UNAUTHORIZED) Long memberId,
            @PathVariable @BindErrorCode(ErrorCode.INVALID_POST_ID) Long postId,
            @RequestBody @Valid CommentRequest request) {
        return ApiResponse.createdEntity(commentService.createComment(memberId, postId, request));
    }

    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글을 오래된 순으로 페이지 단위로 조회합니다.")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentListResponse>> getComments(
            @PathVariable @BindErrorCode(ErrorCode.INVALID_POST_ID) Long postId,
            @RequestParam(defaultValue = "1") @BindErrorCode(ErrorCode.INVALID_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = "10") @BindErrorCode(ErrorCode.INVALID_PAGE_SIZE) int size) {
        return ApiResponse.okEntity(commentService.getComments(postId, page, size));
    }

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @Parameter(description = "작성자 회원 ID (로그인 대체)", required = true)
            @RequestHeader("X-Member-Id") @BindErrorCode(ErrorCode.UNAUTHORIZED) Long memberId,
            @PathVariable @BindErrorCode(ErrorCode.INVALID_COMMENT_ID) Long commentId,
            @RequestBody @Valid CommentRequest request) {
        return ApiResponse.okEntity(commentService.updateComment(memberId, commentId, request));
    }

    @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글을 삭제합니다.")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentDeleteResponse>> deleteComment(
            @Parameter(description = "작성자 회원 ID (로그인 대체)", required = true)
            @RequestHeader("X-Member-Id") @BindErrorCode(ErrorCode.UNAUTHORIZED) Long memberId,
            @PathVariable @BindErrorCode(ErrorCode.INVALID_COMMENT_ID) Long commentId) {
        return ApiResponse.okEntity(commentService.deleteComment(memberId, commentId));
    }
}
