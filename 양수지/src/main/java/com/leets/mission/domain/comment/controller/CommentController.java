package com.leets.mission.domain.comment.controller;

import com.leets.mission.domain.comment.dto.CommentRequestDTO;
import com.leets.mission.domain.comment.dto.CommentResponseDTO;
import com.leets.mission.domain.comment.service.CommentService;
import com.leets.mission.global.apiPayload.ApiResponse;
import com.leets.mission.global.apiPayload.code.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponseDTO.DetailDTO> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentRequestDTO.CreateDTO request
    ) {
        return ApiResponse.of(SuccessStatus.COMMENT_CREATED, commentService.createComment(postId, request));
    }

    // 댓글 목록 조회
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<CommentResponseDTO.ListDTO> getCommentList(@PathVariable Long postId) {
        return ApiResponse.onSuccess(commentService.getCommentList(postId));
    }
}
