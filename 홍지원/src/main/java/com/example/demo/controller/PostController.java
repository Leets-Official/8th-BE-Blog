package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PostDto;
import com.example.demo.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/posts")
@Tag(name = "게시글", description = "게시글 조회 및 관리 API")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @Operation(summary = "게시글 목록 조회", description = "페이지와 정렬 조건에 맞는 게시글 목록을 조회합니다.")
    public ApiResponse<PostDto.PostPage> list(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준", example = "latest") @RequestParam(defaultValue = "latest") String sort) {
        return ApiResponse.success("200", postService.list(page, size, sort));
    }

    @GetMapping("/{post_id}")
    @Operation(summary = "게시글 상세 조회")
    public ApiResponse<PostDto.Detail> detail(@Parameter(description = "게시글 ID", example = "1") @PathVariable("post_id") long postId) {
        return ApiResponse.success("200", postService.detail(postId));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "게시글 작성", description = "로그인한 회원의 게시글을 생성합니다.")
    public ResponseEntity<ApiResponse<PostDto.Saved>> create(
            @Parameter(hidden = true) Principal principal,
            @RequestBody PostDto.Request request) {
        // 사용자 입력 헤더 대신 Security가 인증한 회원 ID를 전달합니다.
        return ResponseEntity.status(201).body(ApiResponse.success("201", postService.create(principal.getName(), request)));
    }

    @PatchMapping("/{post_id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "게시글 수정")
    public ApiResponse<PostDto.Saved> update(@Parameter(description = "게시글 ID", example = "1") @PathVariable("post_id") long postId,
            @Parameter(hidden = true) Principal principal,
            @RequestBody PostDto.Request request) {
        return ApiResponse.success("200", postService.update(postId, principal.getName(), request));
    }

    @DeleteMapping("/{post_id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "게시글 삭제")
    public ApiResponse<PostDto.Deleted> delete(@Parameter(description = "게시글 ID", example = "1") @PathVariable("post_id") long postId,
            @Parameter(hidden = true) Principal principal) {
        return ApiResponse.success("200", postService.delete(postId, principal.getName()));
    }
}
