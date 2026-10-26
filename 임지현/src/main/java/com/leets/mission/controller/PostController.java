package com.leets.mission.controller;

import com.leets.mission.dto.post.PostCreateRequest;
import com.leets.mission.dto.post.PostListResponse;
import com.leets.mission.dto.post.PostResponse;
import com.leets.mission.dto.post.PostUpdateRequest;
import com.leets.mission.global.exception.CustomException;
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

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private static final Set<String> ALLOWED_SORT_TYPES = Set.of("latest", "oldest", "popular");

    @GetMapping
    public ResponseEntity<PostListResponse> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
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

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

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

    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        PostResponse response = postService.updatePost(postId, request, memberId);
        return ResponseEntity.ok(response);
    }

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