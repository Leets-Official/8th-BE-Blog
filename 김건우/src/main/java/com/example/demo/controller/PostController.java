package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.global.ApiResponse;
import com.example.demo.global.SuccessCode;
import com.example.demo.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponse>> getPosts(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page는 0 이상의 숫자여야 합니다.") int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size는 1 이상의 숫자여야 합니다.") int size) {
        return ApiResponse.success(SuccessCode.POST_LIST_OK, postService.getPosts(page, size));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long postId) {
        return ApiResponse.success(SuccessCode.POST_DETAIL_OK, postService.getPost(postId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody PostRequest request) {
        return ApiResponse.success(SuccessCode.POST_CREATED, postService.createPost(request));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long postId, @Valid @RequestBody PostRequest request) {
        return ApiResponse.success(SuccessCode.POST_UPDATED, postService.updatePost(postId, request));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> patchPost(
            @PathVariable Long postId, @Valid @RequestBody PostPatchRequest request) {
        return ApiResponse.success(SuccessCode.POST_UPDATED, postService.patchPost(postId, request));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId, @RequestParam Long userId) {
        postService.deletePost(postId, userId);
        return ApiResponse.success(SuccessCode.POST_DELETED, null);
    }

    // [4주차] 게시글 숨김 (액션형)
    @PostMapping("/{postId}/hide")
    public ResponseEntity<ApiResponse<PostHideResponse>> hidePost(
            @PathVariable Long postId, @RequestParam Long userId) {
        return ApiResponse.success(SuccessCode.POST_HIDDEN, postService.hidePost(postId, userId));
    }
}
