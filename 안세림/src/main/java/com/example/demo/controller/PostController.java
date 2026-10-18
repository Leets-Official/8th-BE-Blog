package com.example.demo.controller;

import com.example.demo.dto.post.PostCreateRequest;
import com.example.demo.dto.post.PostDeleteResponse;
import com.example.demo.dto.post.PostDetailResponse;
import com.example.demo.dto.post.PostListResponse;
import com.example.demo.dto.post.PostUpdateRequest;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ApiResponse<List<PostListResponse>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok("게시글 목록 조회가 완료되었습니다.", postService.getPosts(page, size, keyword));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> getPost(@PathVariable Long postId) {
        return ApiResponse.ok("게시글 조회가 완료되었습니다.", postService.getPost(postId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailResponse>> createPost(@Valid @RequestBody PostCreateRequest request) {
        PostDetailResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("게시글이 성공적으로 등록되었습니다.", response));
    }

    @PatchMapping("/{postId}")
    public ApiResponse<PostDetailResponse> updatePost(@PathVariable Long postId,
                                                      @Valid @RequestBody PostUpdateRequest request) {
        return ApiResponse.ok("게시글이 성공적으로 수정되었습니다.", postService.updatePost(postId, request));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<PostDeleteResponse> deletePost(@PathVariable Long postId,
                                                      @RequestParam Long authorId) {
        return ApiResponse.ok("게시글이 성공적으로 삭제되었습니다.", postService.deletePost(postId, authorId));
    }
}