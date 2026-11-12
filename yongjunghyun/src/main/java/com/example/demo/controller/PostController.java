package com.example.demo.controller;

import com.example.demo.dto.post.*;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 모든 API가 ResponseEntity<ApiResponse<T>>로 응답한다
// (HTTP 상태 코드는 ResponseEntity가, 본문 형식은 ApiResponse가 담당)
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private static final String MEMBER_HEADER = "X-Member-Id";

    private final PostService postService;

    // GET /posts?page=1&size=10  (page는 1부터 시작)
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostSummaryResponse>>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("COMMON200", "게시글 목록 조회에 성공했습니다.",
                postService.getPosts(page, size)));
    }

    // GET /posts/{postId}
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("COMMON200", "게시글 조회에 성공했습니다.",
                postService.getPost(postId)));
    }

    // POST /posts
    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
            @RequestHeader(MEMBER_HEADER) Long memberId,
            @Valid @RequestBody PostCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("COMMON201", "게시글이 작성되었습니다.",
                        postService.createPost(memberId, request)));
    }

    // PATCH /posts/{postId}
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @PathVariable Long postId,
            @RequestHeader(MEMBER_HEADER) Long memberId,
            @Valid @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("COMMON200", "게시글이 수정되었습니다.",
                postService.updatePost(postId, memberId, request)));
    }

    // DELETE /posts/{postId}
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @RequestHeader(MEMBER_HEADER) Long memberId) {
        postService.deletePost(postId, memberId);
        return ResponseEntity.ok(ApiResponse.success("COMMON200", "게시글이 삭제되었습니다.", null));
    }
}
