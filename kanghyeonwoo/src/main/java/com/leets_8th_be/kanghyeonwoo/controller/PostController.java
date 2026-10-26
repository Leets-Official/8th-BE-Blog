package com.leets_8th_be.kanghyeonwoo.controller;

import com.leets_8th_be.kanghyeonwoo.dto.*;
import com.leets_8th_be.kanghyeonwoo.service.PostService;
import com.leets_8th_be.kanghyeonwoo.service.PostSearchService;
import com.leets_8th_be.kanghyeonwoo.service.SearchSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Post", description = "게시글 관련 REST API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final PostSearchService postSearchService;

    @Operation(summary = "게시글 등록", description = "신규 게시글을 등록합니다. (제목은 최대 150자, 본문은 공백 불가)")
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody PostCreateRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("S201", "게시글이 성공적으로 등록되었습니다.", response));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID(postId)로 특정 게시글의 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostById(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable("postId") Long postId) {
        PostDetailResponse response = postService.getPostById(postId);
        return ResponseEntity.ok(ApiResponse.success("S200", "게시글 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "게시글 목록 조회 (검색 및 페이징)", description = "페이징 및 제목 검색 키워드(대소문자 무관)를 기반으로 게시글 목록을 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostSummaryResponse>>> getPosts(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "제목 검색 키워드 (선택)", example = "Spring") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(hidden = true) SearchSession session) {
        List<PostSummaryResponse> responses = postSearchService.getPosts(page, size, keyword, session);
        return ResponseEntity.ok(ApiResponse.success("S200", "게시글 목록 조회가 완료되었습니다.", responses));
    }

    @Operation(summary = "게시글 수정", description = "게시글 작성자 본인만 제목과 내용을 수정할 수 있습니다.")
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable("postId") Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        PostDetailResponse response = postService.updatePost(postId, request);
        return ResponseEntity.ok(ApiResponse.success("S200", "게시글이 성공적으로 수정되었습니다.", response));
    }

    @Operation(summary = "게시글 삭제", description = "게시글 작성자 본인만 삭제할 수 있으며, 연관된 댓글도 함께 연쇄 삭제됩니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDeleteResponse>> deletePost(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable("postId") Long postId,
            @Parameter(description = "작성자 회원 ID", example = "1") @RequestParam("authorId") Long authorId) {
        PostDeleteResponse response = postService.deletePost(postId, authorId);
        return ResponseEntity.ok(ApiResponse.success("S200", "게시글이 성공적으로 삭제되었습니다.", response));
    }
}
