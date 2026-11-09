package com.leets.blog.controller;

import com.leets.blog.dto.post.*;
import com.leets.blog.service.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "게시글", description = "작성·수정·삭제는 로그인이 필요합니다. 비공개 글은 작성자만 조회할 수 있습니다.")
public class PostController {
    private final PostService postService;

    @GetMapping("/list")
    @Operation(summary = "게시글 목록 조회", description = "공개 글과 로그인한 사용자의 비공개 글을 최신 ID 순으로 조회합니다. 삭제되거나 숨김 처리된 글은 제외합니다. 비로그인 시 공개 글만 반환합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 목록. 조회할 글이 없으면 posts는 빈 배열")
    public PostListResponse list(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return postService.list(jwt == null ? null : Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "비공개 글은 작성자의 Access Token이 필요합니다. 숨김 글은 작성자도 조회할 수 없습니다.")
    @ApiResponse(responseCode = "200", description = "게시글 상세 정보")
    public PostResponse detail(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
                               @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return postService.detail(postId, jwt == null ? null : Long.valueOf(jwt.getSubject()));
    }

    @PostMapping
    @Operation(summary = "게시글 작성", description = "로그인한 사용자를 작성자로 저장합니다. isPrivate 생략 시 공개 글로 작성합니다.")
    @ApiResponse(responseCode = "201", description = "게시글 작성 완료. Location 헤더에 상세 경로 반환")
    public ResponseEntity<PostResponse> create(@RequestBody CreatePostRequest request,
                                              @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        PostResponse response = postService.create(new CreatePostRequest(
                Long.valueOf(jwt.getSubject()), request.title(), request.content(), request.isPrivate()));
        return ResponseEntity.created(URI.create("/post/" + response.id())).body(response);
    }

    @PutMapping("/{postId}")
    @Operation(summary = "게시글 수정", description = "작성자만 수정할 수 있습니다. 공개 여부는 유지하며 content 생략 시 기존 내용을 유지합니다.")
    @ApiResponse(responseCode = "200", description = "수정된 게시글 ID, 제목, 내용 반환")
    public UpdatePostResponse update(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
                                     @RequestBody UpdatePostRequest request,
                                     @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return postService.update(postId, new UpdatePostRequest(
                Long.valueOf(jwt.getSubject()), request.title(), request.content()));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "로그인한 사용자가 작성자인지 확인하고 삭제 시각을 기록합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 완료. 응답 본문 없음",
            content = @Content)
    public ResponseEntity<Void> delete(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
                                        @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        postService.delete(postId, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}/hide")
    @Operation(summary = "게시글 숨김", description = "ACTIVE → HIDDEN으로 변경합니다. 요청 본문은 없으며, 숨긴 게시글은 목록과 상세 조회에서 제외됩니다. 중복 숨김 요청은 409를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "숨김 처리 완료")
    public PostStatusResponse hide(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId) {
        return postService.hide(postId);
    }
}
