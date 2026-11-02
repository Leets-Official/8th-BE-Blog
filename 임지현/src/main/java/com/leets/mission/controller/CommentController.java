package com.leets.mission.controller;

import com.leets.mission.dto.comment.CommentCreateRequest;
import com.leets.mission.dto.comment.CommentResponse;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import com.leets.mission.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "댓글", description = "게시글 댓글 조회 및 작성 API")
@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(
            summary = "댓글 목록 조회",
            description = "게시글 ID로 해당 게시글의 댓글 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    @Operation(
            summary = "댓글 상세 조회",
            description = "게시글 ID와 댓글 ID로 해당 댓글의 상세 정보를 조회합니다."
    )
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(commentService.getComment(postId, commentId));
    }

    @Operation(summary = "댓글 작성", description = "요청 회원이 게시글에 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        CommentResponse response =
                commentService.createComment(postId, request, memberId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}