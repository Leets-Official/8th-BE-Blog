package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PostDto;
import com.example.demo.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ApiResponse<PostDto.PostPage> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "latest") String sort) {
        return ApiResponse.success(200, postService.list(page, size, sort));
    }

    @GetMapping("/{post_id}")
    public ApiResponse<PostDto.Detail> detail(@PathVariable("post_id") long postId) {
        return ApiResponse.success("200", postService.detail(postId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostDto.Saved>> create(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody PostDto.Request request) {
        return ResponseEntity.status(201).body(ApiResponse.success("201", postService.create(userId, request)));
    }

    @PatchMapping("/{post_id}")
    public ApiResponse<PostDto.Saved> update(@PathVariable("post_id") long postId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody PostDto.Request request) {
        return ApiResponse.success("200", postService.update(postId, userId, request));
    }

    @DeleteMapping("/{post_id}")
    public ApiResponse<PostDto.Deleted> delete(@PathVariable("post_id") long postId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ApiResponse.success("200", postService.delete(postId, userId));
    }
}
