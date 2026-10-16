package com.leets_8th_be.kanghyeonwoo.controller;

import com.leets_8th_be.kanghyeonwoo.dto.*;
import com.leets_8th_be.kanghyeonwoo.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody PostCreateRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("S201", "게시글이 성공적으로 등록되었습니다.", response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostById(@PathVariable("postId") Long postId) {
        PostDetailResponse response = postService.getPostById(postId);
        return ResponseEntity.ok(ApiResponse.success("S200", "게시글 조회가 완료되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostSummaryResponse>>> getPosts(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword) {
        List<PostSummaryResponse> responses = postService.getPosts(page, size, keyword);
        return ResponseEntity.ok(ApiResponse.success("S200", "게시글 목록 조회가 완료되었습니다.", responses));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @PathVariable("postId") Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        PostDetailResponse response = postService.updatePost(postId, request);
        return ResponseEntity.ok(ApiResponse.success("S200", "게시글이 성공적으로 수정되었습니다.", response));
    }
}
