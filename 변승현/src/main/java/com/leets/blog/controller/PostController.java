package com.leets.blog.controller;

import com.leets.blog.dto.post.*;
import com.leets.blog.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(summary = "게시글 목록 조회", description = "공개 글과 본인의 비공개 글을 최신 ID 순으로 조회합니다. 삭제되거나 숨김 처리된 글은 제외합니다. userId를 생략하면 공개 글만 반환합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 목록. 조회할 글이 없으면 posts는 빈 배열")
    public PostListResponse list(@Parameter(description = "과제용 요청자 ID. 생략하면 공개 글만 조회", example = "1")
                                 @RequestParam(required = false) Long userId) {
        return postService.list(userId);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "비공개 글은 작성자의 userId가 필요합니다. 숨김 글은 작성자도 조회할 수 없습니다.")
    @ApiResponse(responseCode = "200", description = "게시글 상세 정보")
    public PostResponse detail(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
                               @Parameter(description = "비공개 글 조회 시 작성자 ID", example = "1")
                               @RequestParam(required = false) Long userId) {
        return postService.detail(postId, userId);
    }

    @PostMapping
    @Operation(summary = "게시글 작성", description = "기존 사용자의 userId를 입력합니다. isPrivate 생략 시 공개 글로 작성합니다.")
    @ApiResponse(responseCode = "201", description = "게시글 작성 완료. Location 헤더에 상세 경로 반환")
    public ResponseEntity<PostResponse> create(@RequestBody CreatePostRequest request) {
        PostResponse response = postService.create(request);
        return ResponseEntity.created(URI.create("/post/" + response.id())).body(response);
    }

    @PutMapping("/{postId}")
    @Operation(summary = "게시글 수정", description = "작성자만 수정할 수 있습니다. 공개 여부는 유지하며 content 생략 시 기존 내용을 유지합니다.")
    @ApiResponse(responseCode = "200", description = "수정된 게시글 ID, 제목, 내용 반환")
    public UpdatePostResponse update(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId, @RequestBody UpdatePostRequest request) {
        return postService.update(postId, request);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "요청 본문의 userId로 작성자를 확인하고 삭제 시각을 기록합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 완료. 응답 본문 없음",
            content = @Content)
    public ResponseEntity<Void> delete(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId, @RequestBody DeletePostRequest request) {
        postService.delete(postId, request.userId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}/hide")
    @Operation(summary = "게시글 숨김", description = "ACTIVE → HIDDEN으로 변경합니다. 요청 본문은 없으며, 숨긴 게시글은 목록과 상세 조회에서 제외됩니다. 중복 숨김 요청은 409를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "숨김 처리 완료")
    public PostStatusResponse hide(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId) {
        return postService.hide(postId);
    }
}
