package com.example.demo.controller.docs;

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
    LikeResponse likePost(
        @Parameter(description = "게시글 ID (양수)", example = "1")
        Long postId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );

    @Operation(summary = "게시글 좋아요 취소", description = "게시글에 등록한 좋아요를 취소합니다. 등록된 좋아요가 없어도 처리됩니다.")
    @ApiResponse(responseCode = "204", description = "성공", content = @Content)
    ResponseEntity<Void> unlikePost(
        @Parameter(description = "게시글 ID (양수)", example = "1")
        Long postId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );

    @Operation(summary = "댓글 좋아요", description = "댓글에 좋아요를 등록하고 전체 좋아요 수와 좋아요 여부를 반환합니다. 이미 등록한 경우 좋아요 수는 증가하지 않습니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    LikeResponse likeComment(
        @Parameter(description = "댓글 ID (양수)", example = "1")
        Long commentId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );

    @Operation(summary = "댓글 좋아요 취소", description = "댓글에 등록한 좋아요를 취소합니다. 등록된 좋아요가 없어도 처리됩니다.")
    @ApiResponse(responseCode = "204", description = "성공", content = @Content)
    ResponseEntity<Void> unlikeComment(
        @Parameter(description = "댓글 ID (양수)", example = "1")
        Long commentId,
        @Parameter(description = "요청 회원 ID. 기존 회원의 ID를 입력합니다.", example = "1")
        Long userId
    );
}
