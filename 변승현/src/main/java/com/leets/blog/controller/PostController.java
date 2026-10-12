package com.leets.blog.controller;

import com.leets.blog.dto.post.*;
import com.leets.blog.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("mysql")
@RequestMapping("/post")
@RequiredArgsConstructor
@Tag(name = "게시글", description = "userId는 인증 대신 사용하는 과제용 사용자 식별값입니다.")
public class PostController {
    private final PostService postService;

    @GetMapping("/list")
    @Operation(summary = "게시글 목록 조회", description = "공개 글과 본인의 비공개 글을 최신 ID 순으로 조회합니다. 삭제된 글은 제외합니다.")
    public PostListResponse list(@Parameter(description = "생략하면 공개 글만 조회")
                                 @RequestParam(required = false) Long userId) {
        return postService.list(userId);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "비공개 글은 작성자의 userId가 필요합니다.")
    public PostResponse detail(@PathVariable Long postId,
                               @RequestParam(required = false) Long userId) {
        return postService.detail(postId, userId);
    }

    @PostMapping
    @Operation(summary = "게시글 작성", description = "기존 사용자의 userId를 입력합니다. isPrivate 생략 시 공개 글로 작성합니다.")
    public ResponseEntity<PostResponse> create(@RequestBody CreatePostRequest request) {
        PostResponse response = postService.create(request);
        return ResponseEntity.created(URI.create("/post/" + response.id())).body(response);
    }

    @PutMapping("/{postId}")
    @Operation(summary = "게시글 수정", description = "작성자만 수정할 수 있습니다. 공개 여부는 유지하며 content 생략 시 기존 내용을 유지합니다.")
    public UpdatePostResponse update(@PathVariable Long postId, @RequestBody UpdatePostRequest request) {
        return postService.update(postId, request);
    }
}
