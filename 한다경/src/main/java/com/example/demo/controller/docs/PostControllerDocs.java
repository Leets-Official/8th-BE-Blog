package com.example.demo.controller.docs;

import com.example.demo.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    PostListResponse findPosts();

    @Operation(summary = "게시글 상세 조회", description = "게시글의 상세 내용을 조회합니다. 비공개 게시글은 작성자만 조회할 수 있으며, 숨김 및 삭제된 게시글은 조회할 수 없습니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "게시글 ID가 양수가 아니거나 요청값 형식이 잘못된 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "숨김 게시글 또는 다른 회원의 비공개 게시글",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "게시글이 없거나 삭제된 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    PostResponse findPost(
        @Parameter(description = "게시글 ID (양수)", example = "1")
        Long postId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );

    @Operation(summary = "게시글 작성", description = "제목과 본문으로 게시글을 작성합니다. 비공개 여부를 지정할 수 있으며, 지정하지 않으면 공개 게시글로 생성합니다.")
    @ApiResponse(responseCode = "201", description = "성공")
    @ApiResponse(responseCode = "400", description = "요청값 형식 오류 또는 필수 X-User-Id 헤더 누락 또는 제목·본문 검증 실패",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "회원이 없거나 삭제된 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<PostResponse> createPost(
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId,
        PostCreateRequest request
    );

    @Operation(summary = "게시글 수정", description = "작성자가 게시글의 제목과 본문을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "요청값 형식 오류 또는 필수 X-User-Id 헤더 누락 또는 게시글 ID·제목·본문 검증 실패",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "게시글 작성자가 아닌 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "게시글이 없거나 삭제된 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "동시 수정 충돌",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    PostResponse updatePost(
        @Parameter(description = "게시글 ID (양수)", example = "1")
        Long postId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId,
        PostUpdateRequest request
    );

    @Operation(summary = "게시글 삭제", description = "작성자가 게시글을 삭제 처리하여 조회할 수 없게 합니다.")
    @ApiResponse(responseCode = "204", description = "성공", content = @Content)
    @ApiResponse(responseCode = "400", description = "요청값 형식 오류 또는 필수 X-User-Id 헤더 누락 또는 게시글 ID가 양수가 아닌 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "게시글 작성자가 아닌 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "게시글이 없거나 삭제된 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "동시 수정 충돌",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Void> deletePost(
        @Parameter(description = "게시글 ID (양수)", example = "1")
        Long postId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );
}
