package com.leets.blog.controller;

import com.leets.blog.dto.post.PostCreateRequest;
import com.leets.blog.dto.post.PostDeleteResponse;
import com.leets.blog.dto.post.PostDetailResponse;
import com.leets.blog.dto.post.PostListResponse;
import com.leets.blog.dto.post.PostUpdateRequest;
import com.leets.blog.global.response.ApiResponse;
import com.leets.blog.global.response.SuccessCode;
import com.leets.blog.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostListResponse>>> getPosts(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page는 1 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size,
            @RequestParam(required = false) String keyword) {
        return respond(SuccessCode.POST_LIST_FOUND, postService.getPosts(page, size, keyword));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable Long postId) {
        return respond(SuccessCode.POST_FOUND, postService.getPost(postId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailResponse>> createPost(@Valid @RequestBody PostCreateRequest request) {
        return respond(SuccessCode.POST_CREATED, postService.createPost(request));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        return respond(SuccessCode.POST_UPDATED, postService.updatePost(postId, request));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDeleteResponse>> deletePost(
            @PathVariable Long postId,
            @RequestParam Long authorId) {
        return respond(SuccessCode.POST_DELETED, postService.deletePost(postId, authorId));
    }

    @PatchMapping("/{postId}/hide")
    public ResponseEntity<ApiResponse<PostDetailResponse>> hidePost(
            @PathVariable Long postId,
            @RequestParam Long authorId) {
        return respond(SuccessCode.POST_HIDDEN, postService.hidePost(postId, authorId));
    }

    private <T> ResponseEntity<ApiResponse<T>> respond(SuccessCode successCode, T data) {
        return ResponseEntity.status(successCode.getStatus()).body(ApiResponse.success(successCode, data));
    }
}
