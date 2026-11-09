package com.leets.blog.controller;

import com.leets.blog.dto.post.PostCreateRequest;
import com.leets.blog.dto.post.PostDeleteResponse;
import com.leets.blog.dto.post.PostDetailResponse;
import com.leets.blog.dto.post.PostListResponse;
import com.leets.blog.dto.post.PostUpdateRequest;
import com.leets.blog.global.response.ApiResponse;
import com.leets.blog.global.response.SuccessCode;
import com.leets.blog.global.swagger.ApiErrorCodes;
import com.leets.blog.global.swagger.SwaggerConfig;
import com.leets.blog.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.leets.blog.global.exception.ErrorCode.INVALID_INPUT;
import static com.leets.blog.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.leets.blog.global.exception.ErrorCode.POST_ALREADY_HIDDEN;
import static com.leets.blog.global.exception.ErrorCode.POST_FORBIDDEN;
import static com.leets.blog.global.exception.ErrorCode.POST_NOT_FOUND;
import static com.leets.blog.global.exception.ErrorCode.TYPE_MISMATCH;
import static com.leets.blog.global.exception.ErrorCode.UNAUTHORIZED;

@Tag(name = "게시글", description = "게시글 작성, 조회, 수정, 삭제, 숨김 API")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회",
            description = "최신순으로 게시글 목록을 조회합니다. keyword를 보내면 제목에 keyword가 포함된 글만 조회합니다. 숨김 처리된 글은 보이지 않습니다.")
    @ApiErrorCodes({INVALID_INPUT})
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostListResponse>>> getPosts(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page는 1 이상이어야 합니다.") int page,
            @Parameter(description = "한 페이지에 보여줄 게시글 수 (1~100)", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size,
            @Parameter(description = "제목 검색어 (선택)", example = "회고")
            @RequestParam(required = false) String keyword) {
        return respond(SuccessCode.POST_LIST_FOUND, postService.getPosts(page, size, keyword));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 하나의 내용을 조회합니다. 숨김 처리된 글은 404를 반환합니다.")
    @ApiErrorCodes({TYPE_MISMATCH, POST_NOT_FOUND})
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId) {
        return respond(SuccessCode.POST_FOUND, postService.getPost(postId));
    }

    @Operation(summary = "게시글 작성",
            description = "로그인한 회원이 새 게시글을 작성합니다. 작성된 게시글은 ACTIVE 상태로 생성됩니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @ApiErrorCodes({INVALID_INPUT, UNAUTHORIZED, MEMBER_NOT_FOUND})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailResponse>> createPost(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PostCreateRequest request) {
        return respond(SuccessCode.POST_CREATED, postService.createPost(memberId, request));
    }

    @Operation(summary = "게시글 수정", description = "작성자 본인만 제목과 본문을 수정할 수 있습니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @ApiErrorCodes({INVALID_INPUT, UNAUTHORIZED, POST_FORBIDDEN, POST_NOT_FOUND})
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        return respond(SuccessCode.POST_UPDATED, postService.updatePost(postId, memberId, request));
    }

    @Operation(summary = "게시글 삭제", description = "작성자 본인만 삭제할 수 있습니다. 게시글에 달린 댓글도 함께 삭제됩니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @ApiErrorCodes({UNAUTHORIZED, POST_FORBIDDEN, POST_NOT_FOUND})
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDeleteResponse>> deletePost(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId) {
        return respond(SuccessCode.POST_DELETED, postService.deletePost(postId, memberId));
    }

    @Operation(summary = "게시글 숨김",
            description = "작성자 본인이 게시글을 숨깁니다. 상태가 ACTIVE에서 HIDDEN으로 바뀌고, 이후 목록/상세 조회에서 보이지 않습니다. 이미 숨긴 글은 409를 반환합니다.")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @ApiErrorCodes({UNAUTHORIZED, POST_FORBIDDEN, POST_NOT_FOUND, POST_ALREADY_HIDDEN})
    @PatchMapping("/{postId}/hide")
    public ResponseEntity<ApiResponse<PostDetailResponse>> hidePost(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId) {
        return respond(SuccessCode.POST_HIDDEN, postService.hidePost(postId, memberId));
    }

    private <T> ResponseEntity<ApiResponse<T>> respond(SuccessCode successCode, T data) {
        return ResponseEntity.status(successCode.getStatus()).body(ApiResponse.success(successCode, data));
    }
}
