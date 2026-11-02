package com.leets.mission.domain.comment.controller;

import com.leets.mission.domain.comment.dto.CommentRequestDTO;
import com.leets.mission.domain.comment.dto.CommentResponseDTO;
import com.leets.mission.domain.comment.service.CommentService;
import com.leets.mission.global.apiPayload.ApiResponse;
import com.leets.mission.global.apiPayload.ErrorResponseSchema;
import com.leets.mission.global.apiPayload.code.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "댓글", description = "댓글 API")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @Operation(summary = "댓글 생성")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "생성 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: 입력값 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "POST_404_1: 게시글 없음 / USER_404_1: 사용자 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponseDTO.DetailDTO> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentRequestDTO.CreateDTO request
    ) {
        return ApiResponse.of(SuccessStatus.COMMENT_CREATED, commentService.createComment(postId, request));
    }

    // 댓글 목록 조회
    @Operation(summary = "댓글 목록 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: postId 형식 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "POST_404_1: 게시글 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<CommentResponseDTO.ListDTO> getCommentList(@PathVariable Long postId) {
        return ApiResponse.onSuccess(commentService.getCommentList(postId));
    }
}
