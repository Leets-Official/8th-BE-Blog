package com.example.demo.controller;

import com.example.demo.dto.request.PostCreateRequest;
import com.example.demo.dto.request.PostUpdateRequest;
import com.example.demo.dto.response.PostListResponse;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/post/list")
    public PostListResponse findPosts() {
        return postService.findPosts();
    }

    @GetMapping("/post/{postId}")
    public PostResponse findPost(
            @PathVariable Long postId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        return postService.findPost(postId, userId);
    }

    @PostMapping("/post")
    public ResponseEntity<PostResponse> createPost(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PostCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.createPost(userId, request));
    }

    @PutMapping("/post/{postId}")
    public PostResponse updatePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        return postService.updatePost(postId, userId, request);
    }

    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        postService.deletePost(postId, userId);

        return ResponseEntity.noContent().build();
    }
}
