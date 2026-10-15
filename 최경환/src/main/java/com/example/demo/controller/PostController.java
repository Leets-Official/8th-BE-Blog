package com.example.demo.controller;

import com.example.demo.dto.PostResponse;
import com.example.demo.dto.PostWriteRequest;
import com.example.demo.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 게시글 목록 조회
    @GetMapping("/list")
    public List<PostResponse> findAll(@RequestParam(required = false) Long userId) {
        return postService.findAll(userId);
    }

    // 게시글 작성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(
            @RequestParam Long userId,
            @Valid @RequestBody PostWriteRequest request
    ) {
        return postService.create(userId, request);
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public PostResponse findById(
            @PathVariable Long postId,
            @RequestParam(required = false) Long userId
    ) {
        return postService.findById(postId, userId);
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public PostResponse update(
            @PathVariable Long postId,
            @RequestParam Long userId,
            @Valid @RequestBody PostWriteRequest request
    ) {
        return postService.update(postId, userId, request);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        postService.delete(postId, userId);
    }
}
