package com.leets.mission.controller;

import com.leets.mission.dto.post.PostCreateRequest;
import com.leets.mission.dto.post.PostListResponse;
import com.leets.mission.dto.post.PostResponse;
import com.leets.mission.dto.post.PostUpdateRequest;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorResponse;
import com.leets.mission.global.exception.ErrorCode;
import com.leets.mission.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "게시글", description = "게시글 목록/상세 조회, 작성/수정/삭제 API")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private static final Set<String> ALLOWED_SORT_TYPES = Set.of("latest", "oldest", "popular");

    @Operation(
            summary = "게시글 목록 조회",
            description = "페이지 번호, 페이지 크기, 정렬 기준으로 게시글 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<PostListResponse> getPosts(
            @Parameter(description = "페이지 번호, 1부터 시작", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "페이지 크기 (1~50)", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "정렬 기준: latest, oldest, popular", example = "latest")
            @RequestParam(defaultValue = "latest") String sort
    ) {
        if (page < 1) {
            throw new CustomException(ErrorCode.INVALID_PAGE_NUMBER);
        }
        if (size < 1 || size > 50) {
            throw new CustomException(ErrorCode.INVALID_PAGE_SIZE);
        }
        if (!ALLOWED_SORT_TYPES.contains(sort)) {
            throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
        }

        Sort sortCondition = switch (sort) {
            case "oldest" -> Sort.by("createdAt").ascending();
            case "popular" -> Sort.by("viewCount").descending();
            default -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(page - 1, size, sortCondition);

        return ResponseEntity.ok(postService.getPosts(pageable));
    }
    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글 ID로 해당 게시글의 상세 정보를 조회합니다."
    )
   @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다.")
    @PostMapping
    public ResponseEntity<Void> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        Long createdPostId = postService.createPost(request, memberId);
        URI location = URI.create("/posts/" + createdPostId);

        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "게시글 수정", description = "본인 게시글의 제목과 본문을 수정합니다.")
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "조회할 게시글 ID", example = "1")
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request,
            @Parameter(description = "요청 회원 ID", example = "1")
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        PostResponse response = postService.updatePost(postId, request, memberId);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "게시글 삭제", description = "본인 게시글의 제목과 본문을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        postService.deletePost(postId, memberId);

        return ResponseEntity.noContent().build();
    }
}