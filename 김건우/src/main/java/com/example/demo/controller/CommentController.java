package com.example.demo.controller;

import com.example.demo.dto.CommentRequest;
import com.example.demo.dto.CommentResponse;
import com.example.demo.global.ApiResponse;
import com.example.demo.global.SuccessCode;
import com.example.demo.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long postId, @Valid @RequestBody CommentRequest request) {
        return ApiResponse.success(SuccessCode.COMMENT_CREATED, commentService.createComment(postId, request));
    }

    // 댓글 채택: 액션형
    @PostMapping("/{commentId}/accept")
    public ResponseEntity<ApiResponse<CommentResponse>> acceptComment(
            @PathVariable Long postId, @PathVariable Long commentId, @RequestParam Long userId) {
        return ApiResponse.success(SuccessCode.COMMENT_ACCEPTED,
                commentService.acceptComment(postId, commentId, userId));
    }
}
