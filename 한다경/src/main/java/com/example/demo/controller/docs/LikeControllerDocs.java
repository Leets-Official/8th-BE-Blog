package com.example.demo.controller.docs;

import com.example.demo.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import com.example.demo.dto.response.LikeResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "좋아요", description = "게시글 및 댓글 좋아요 관리")
public interface LikeControllerDocs {

    @Operation(summary = "게시글 좋아요", description = "게시글에 좋아요를 등록하고 전체 좋아요 수와 좋아요 여부를 반환합니다. 이미 등록한 경우 좋아요 수는 증가하지 않습니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "요청값 형식 오류 또는 필수 X-User-Id 헤더 누락 또는 대상 ID가 양수가 아닌 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "숨김 게시글 또는 다른 회원의 비공개 게시글",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "회원 또는 게시글이 없거나 삭제된 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "동시 수정 충돌",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    LikeResponse likePost(
        @Parameter(description = "게시글 ID (양수)", example = "1")
        Long postId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );

    @Operation(summary = "게시글 좋아요 취소", description = "게시글에 등록한 좋아요를 취소합니다. 등록된 좋아요가 없어도 처리됩니다.")
    @ApiResponse(responseCode = "204", description = "성공", content = @Content)
    @ApiResponse(responseCode = "400", description = "요청값 형식 오류 또는 필수 X-User-Id 헤더 누락 또는 대상 ID가 양수가 아닌 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "회원이 없거나 삭제된 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "동시 수정 충돌",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Void> unlikePost(
        @Parameter(description = "게시글 ID (양수)", example = "1")
        Long postId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );

    @Operation(summary = "댓글 좋아요", description = "댓글에 좋아요를 등록하고 전체 좋아요 수와 좋아요 여부를 반환합니다. 이미 등록한 경우 좋아요 수는 증가하지 않습니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "요청값 형식 오류 또는 필수 X-User-Id 헤더 누락 또는 대상 ID가 양수가 아닌 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "댓글 또는 소속 게시글이 숨김 상태이거나 비공개 게시글에 접근할 수 없는 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "회원·댓글·소속 게시글이 없거나 삭제된 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "동시 수정 충돌",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    LikeResponse likeComment(
        @Parameter(description = "댓글 ID (양수)", example = "1")
        Long commentId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );

    @Operation(summary = "댓글 좋아요 취소", description = "댓글에 등록한 좋아요를 취소합니다. 등록된 좋아요가 없어도 처리됩니다.")
    @ApiResponse(responseCode = "204", description = "성공", content = @Content)
    @ApiResponse(responseCode = "400", description = "요청값 형식 오류 또는 필수 X-User-Id 헤더 누락 또는 대상 ID가 양수가 아닌 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "회원이 없거나 삭제된 경우",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "동시 수정 충돌",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Void> unlikeComment(
        @Parameter(description = "댓글 ID (양수)", example = "1")
        Long commentId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );
}
