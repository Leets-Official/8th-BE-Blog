package com.leets.choyeji.controller;

import com.leets.choyeji.auth.JwtMemberResolver;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final JwtMemberResolver jwtMemberResolver;

    @Operation(summary = "댓글 작성")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ApiResponse<CommentResponse> create(@PathVariable Long postId,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CommentRequest request) {
        return ApiResponse.success(commentService.create(jwtMemberResolver.resolve(authorization), postId, request));
    }

    @Operation(summary = "댓글 목록 조회")
    @GetMapping
    public ApiResponse<List<CommentResponse>> list(@PathVariable Long postId) {
        return ApiResponse.success(commentService.list(postId));
    }
}
