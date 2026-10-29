package com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.UpdatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.UpdatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.DeletePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.DeletePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiPaths;
import com.leets_8th_be.kanghyeonwoo.dto.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.dto.PostCreateRequest;
import com.leets_8th_be.kanghyeonwoo.dto.PostDeleteResponse;
import com.leets_8th_be.kanghyeonwoo.dto.PostDetailResponse;
import com.leets_8th_be.kanghyeonwoo.dto.PostResponse;
import com.leets_8th_be.kanghyeonwoo.dto.PostUpdateRequest;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Post", description = "게시글 관련 REST API")
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API_V1 + "/posts")
public class PostController {

    private final CreatePostUseCase createPost;
    private final GetPostUseCase getPost;
    private final UpdatePostUseCase updatePost;
    private final DeletePostUseCase deletePost;

    @Operation(summary = "게시글 등록", description = "신규 게시글을 등록합니다. (제목은 최대 150자, 본문은 공백 불가)")
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody PostCreateRequest request) {
        PostView post = createPost.create(new CreatePostCommand(request.getTitle(), request.getContent(),
                new UserId(request.getAuthorId())));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("S201", "게시글이 성공적으로 등록되었습니다.", toCreateResponse(post)));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID(postId)로 특정 게시글의 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostById(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable("postId") Long postId) {
        PostView post = getPost.get(new PostId(postId));
        return ResponseEntity.ok(ApiResponse.success("S200", "게시글 조회가 완료되었습니다.", toDetailResponse(post)));
    }

    @Operation(summary = "게시글 수정", description = "게시글 작성자 본인만 제목과 내용을 수정할 수 있습니다.")
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable("postId") Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        PostView updated = updatePost.update(new UpdatePostCommand(new PostId(postId),
                new UserId(request.getAuthorId()), request.getTitle(), request.getContent()));
        PostDetailResponse response = toDetailResponse(updated);
        return ResponseEntity.ok(ApiResponse.success("S200", "게시글이 성공적으로 수정되었습니다.", response));
    }

    @Operation(summary = "게시글 삭제", description = "작성자의 게시글을 DELETED 상태로 전환합니다. 게시글과 연관 댓글·신고 스냅샷은 보존되며 공개 조회에서는 제외됩니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDeleteResponse>> deletePost(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable("postId") Long postId,
            @Parameter(description = "작성자 회원 ID", example = "1") @RequestParam("authorId") Long authorId) {
        PostId deletedId = deletePost.delete(new DeletePostCommand(new PostId(postId), new UserId(authorId)));
        PostDeleteResponse response = new PostDeleteResponse(deletedId.value());
        return ResponseEntity.ok(ApiResponse.success("S200", "게시글이 성공적으로 삭제되었습니다.", response));
    }

    private static PostResponse toCreateResponse(PostView post) {
        return new PostResponse(post.id().value(), post.title(), post.content(),
                post.authorId().value(), post.createdAt());
    }

    private static PostDetailResponse toDetailResponse(PostView post) {
        return new PostDetailResponse(post.id().value(), post.title(), post.content(),
                post.authorId().value(), post.createdAt(), post.updatedAt());
    }
}
