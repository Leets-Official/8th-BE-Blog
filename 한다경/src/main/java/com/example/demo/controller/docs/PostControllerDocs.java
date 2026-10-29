package com.example.demo.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import com.example.demo.dto.request.PostCreateRequest;
import com.example.demo.dto.request.PostUpdateRequest;
import com.example.demo.dto.response.PostListResponse;
import com.example.demo.dto.response.PostResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "게시글", description = "게시글 조회, 작성, 수정 및 삭제")
public interface PostControllerDocs {

    @Operation(summary = "게시글 목록 조회", description = "공개 상태이며 숨김 및 삭제되지 않은 게시글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    PostListResponse findPosts();

    @Operation(summary = "게시글 상세 조회", description = "게시글의 상세 내용을 조회합니다. 비공개 게시글은 작성자만 조회할 수 있으며, 숨김 및 삭제된 게시글은 조회할 수 없습니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    PostResponse findPost(
        @Parameter(description = "게시글 ID (양수)", example = "1")
        Long postId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );

    @Operation(summary = "게시글 작성", description = "제목과 본문으로 게시글을 작성합니다. 비공개 여부를 지정할 수 있으며, 지정하지 않으면 공개 게시글로 생성합니다.")
    @ApiResponse(responseCode = "201", description = "성공")
    ResponseEntity<PostResponse> createPost(
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId,
        PostCreateRequest request
    );

    @Operation(summary = "게시글 수정", description = "작성자가 게시글의 제목과 본문을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    PostResponse updatePost(
        @Parameter(description = "게시글 ID (양수)", example = "1")
        Long postId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId,
        PostUpdateRequest request
    );

    @Operation(summary = "게시글 삭제", description = "작성자가 게시글을 삭제 처리하여 조회할 수 없게 합니다.")
    @ApiResponse(responseCode = "204", description = "성공", content = @Content)
    ResponseEntity<Void> deletePost(
        @Parameter(description = "게시글 ID (양수)", example = "1")
        Long postId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );
}
