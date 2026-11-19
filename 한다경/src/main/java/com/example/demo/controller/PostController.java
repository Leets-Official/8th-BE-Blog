package com.example.demo.controller;

import com.example.demo.controller.docs.PostControllerDocs;

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
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class PostController implements PostControllerDocs {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Override
    @GetMapping("/posts")
    public PostListResponse findPosts() {
        return postService.findPosts();
    }

    @Override
    @GetMapping("/posts/{postId}")
    public PostResponse findPost(
            @PathVariable Long postId,
            Principal principal
    ) {
        return postService.findPost(postId, principal == null ? null : Long.valueOf(principal.getName()));
    }

    @Override
    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(
            Principal principal,
            @Valid @RequestBody PostCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.createPost(Long.valueOf(principal.getName()), request));
    }

    @Override
    @PutMapping("/posts/{postId}")
    public PostResponse updatePost(
            @PathVariable Long postId,
            Principal principal,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        return postService.updatePost(postId, Long.valueOf(principal.getName()), request);
    }

    @Override
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            Principal principal
    ) {
        postService.deletePost(postId, Long.valueOf(principal.getName()));

        return ResponseEntity.noContent().build();
    }
}
