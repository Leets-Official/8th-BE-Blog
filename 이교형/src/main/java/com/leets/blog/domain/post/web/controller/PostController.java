package com.leets.blog.domain.post.web.controller;

import com.leets.blog.common.exception.BindErrorCode;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.common.response.ApiResponse;
import com.leets.blog.domain.post.service.PostService;
import com.leets.blog.domain.post.web.dto.request.PostCreateRequest;
import com.leets.blog.domain.post.web.dto.request.PostUpdateRequest;
import com.leets.blog.domain.post.web.dto.response.PostDeleteResponse;
import com.leets.blog.domain.post.web.dto.response.PostDetailResponse;
import com.leets.blog.domain.post.web.dto.response.PostListResponse;
import com.leets.blog.domain.post.web.dto.response.PostResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Post API", description = "게시글 CRUD API")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회", description = "삭제되지 않은 게시글을 페이지 단위로 조회합니다. sort: latest, oldest, popular")
    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponse>> getPosts(
            @RequestParam(defaultValue = "1") @BindErrorCode(ErrorCode.INVALID_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = "10") @BindErrorCode(ErrorCode.INVALID_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "latest") String sort) {
        return ApiResponse.okEntity(postService.getPosts(page, size, sort));
    }

    @Operation(summary = "게시글 상세 조회", description = "삭제되지 않은 게시글 하나를 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(
            @PathVariable @BindErrorCode(ErrorCode.INVALID_POST_ID) Long postId) {
        return ApiResponse.okEntity(postService.getPost(postId));
    }

    @Operation(summary = "게시글 생성", description = "게시글을 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Parameter(description = "작성자 회원 ID (로그인 대체)", required = true)
            @RequestHeader("X-Member-Id") @BindErrorCode(ErrorCode.UNAUTHORIZED) Long memberId,
            @RequestBody @Valid PostCreateRequest request) {
        return ApiResponse.createdEntity(postService.createPost(memberId, request));
    }

    @Operation(summary = "게시글 수정", description = "본인이 작성한 게시글을 수정합니다. 바꿀 필드(title, content)만 보내면 됩니다.")
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @Parameter(description = "작성자 회원 ID (로그인 대체)", required = true)
            @RequestHeader("X-Member-Id") @BindErrorCode(ErrorCode.UNAUTHORIZED) Long memberId,
            @PathVariable @BindErrorCode(ErrorCode.INVALID_POST_ID) Long postId,
            @RequestBody @Valid PostUpdateRequest request) {
        return ApiResponse.okEntity(postService.updatePost(memberId, postId, request));
    }

    @Operation(summary = "게시글 삭제", description = "본인이 작성한 게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDeleteResponse>> deletePost(
            @Parameter(description = "작성자 회원 ID (로그인 대체)", required = true)
            @RequestHeader("X-Member-Id") @BindErrorCode(ErrorCode.UNAUTHORIZED) Long memberId,
            @PathVariable @BindErrorCode(ErrorCode.INVALID_POST_ID) Long postId) {
        return ApiResponse.okEntity(postService.deletePost(memberId, postId));
    }
}
