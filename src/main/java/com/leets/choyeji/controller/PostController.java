package com.leets.choyeji.controller;

import com.leets.choyeji.config.ApiErrorResponses;
import static com.leets.choyeji.config.SwaggerError.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.leets.choyeji.dto.ApiResponse;
import com.leets.choyeji.dto.PostListResponse;
import com.leets.choyeji.dto.PostRequest;
import com.leets.choyeji.dto.PostResponse;
import com.leets.choyeji.dto.PostUpdateResponse;
import com.leets.choyeji.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @Operation(summary = "게시글 조회")
    @ApiErrorResponses({INVALID_REQUEST, POST_NOT_FOUND})
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> get(@PathVariable Long postId) {
        return ApiResponse.success(postService.get(postId));
    }

    @Operation(summary = "게시글 목록 조회")
    @ApiErrorResponses({INVALID_REQUEST})
    @GetMapping
    public ApiResponse<PostListResponse> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(postService.list(page, size));
    }

    @Operation(summary = "게시글 생성")
    @ApiErrorResponses({INVALID_REQUEST, POST_INPUT, UNAUTHORIZED})
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ApiResponse<PostResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PostRequest request) {
        return ApiResponse.success(postService.create(memberId, request));
    }

    @Operation(summary = "게시글 수정")
    @ApiErrorResponses({INVALID_REQUEST, POST_INPUT, UNAUTHORIZED, POST_FORBIDDEN, POST_NOT_FOUND})
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{postId}")
    public ApiResponse<PostUpdateResponse> update(@PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PostRequest request) {
        return ApiResponse.success(postService.update(memberId, postId, request));
    }

    @Operation(summary = "게시글 삭제")
    @ApiErrorResponses({INVALID_REQUEST, UNAUTHORIZED, POST_FORBIDDEN, POST_NOT_FOUND})
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @PathVariable Long postId) {
        postService.delete(memberId, postId);
        return ApiResponse.success(null);
    }
}
