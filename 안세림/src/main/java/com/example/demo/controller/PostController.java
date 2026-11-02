package com.example.demo.controller;

import com.example.demo.dto.post.PostCreateRequest;
import com.example.demo.dto.post.PostDeleteResponse;
import com.example.demo.dto.post.PostDetailResponse;
import com.example.demo.dto.post.PostHideRequest;
import com.example.demo.dto.post.PostListResponse;
import com.example.demo.dto.post.PostStatusResponse;
import com.example.demo.dto.post.PostUpdateRequest;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Post", description = "게시글 API")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회", description = "숨김 처리되지 않은 게시글을 최신순으로 조회합니다. page는 1부터 시작하고, keyword가 있으면 제목으로 검색합니다.")
    @GetMapping
    public ApiResponse<List<PostListResponse>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok("게시글 목록 조회가 완료되었습니다.", postService.getPosts(page, size, keyword));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 상세 내용을 조회합니다. 없거나 숨김 처리된 글이면 404(POST4001)를 반환합니다.")
    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> getPost(@PathVariable Long postId) {
        return ApiResponse.ok("게시글 조회가 완료되었습니다.", postService.getPost(postId));
    }

    @Operation(summary = "게시글 작성", description = "제목, 본문, 작성자 ID로 게시글을 작성합니다. 성공 시 201, 입력값이 잘못되면 400을 반환합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailResponse>> createPost(@Valid @RequestBody PostCreateRequest request) {
        PostDetailResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("게시글이 성공적으로 등록되었습니다.", response));
    }

    @Operation(summary = "게시글 수정", description = "작성자 본인만 수정할 수 있고, 보낸 값만 수정됩니다. 작성자가 아니면 403(POST4003)을 반환합니다.")
    @PatchMapping("/{postId}")
    public ApiResponse<PostDetailResponse> updatePost(@PathVariable Long postId,
                                                      @Valid @RequestBody PostUpdateRequest request) {
        return ApiResponse.ok("게시글이 성공적으로 수정되었습니다.", postService.updatePost(postId, request));
    }

    @Operation(summary = "게시글 숨김", description = "작성자 본인이 게시글을 숨깁니다(ACTIVE → HIDDEN). 이미 숨긴 글이면 409(POST4091)를 반환합니다.")
    @PostMapping("/{postId}/hide")
    public ApiResponse<PostStatusResponse> hidePost(@PathVariable Long postId,
                                                    @Valid @RequestBody PostHideRequest request) {
        return ApiResponse.ok("게시글이 숨김 처리되었습니다.", postService.hidePost(postId, request));
    }

    @Operation(summary = "게시글 삭제", description = "작성자 본인만 삭제할 수 있습니다. 게시글과 댓글에 달린 신고도 함께 삭제됩니다.")
    @DeleteMapping("/{postId}")
    public ApiResponse<PostDeleteResponse> deletePost(@PathVariable Long postId,
                                                      @RequestParam Long authorId) {
        return ApiResponse.ok("게시글이 성공적으로 삭제되었습니다.", postService.deletePost(postId, authorId));
    }
}