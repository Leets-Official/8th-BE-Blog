package com.leets.choyeji.controller;

import com.leets.choyeji.config.ApiErrorResponses;
import static com.leets.choyeji.config.SwaggerError.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.leets.choyeji.dto.ApiResponse;
import com.leets.choyeji.dto.CommentRequest;
import com.leets.choyeji.dto.CommentResponse;
import com.leets.choyeji.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @Operation(summary = "댓글 작성")
    @ApiErrorResponses({INVALID_REQUEST, COMMENT_INPUT, UNAUTHORIZED, POST_NOT_FOUND})
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ApiResponse<CommentResponse> create(@PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CommentRequest request) {
        return ApiResponse.success(commentService.create(memberId, postId, request));
    }

    @Operation(summary = "댓글 목록 조회")
    @ApiErrorResponses({INVALID_REQUEST, POST_NOT_FOUND})
    @GetMapping
    public ApiResponse<List<CommentResponse>> list(@PathVariable Long postId) {
        return ApiResponse.success(commentService.list(postId));
    }
}
