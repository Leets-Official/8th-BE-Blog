package com.leets_8th_be.kanghyeonwoo.controller;

import com.leets_8th_be.kanghyeonwoo.dto.*;
import com.leets_8th_be.kanghyeonwoo.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 목록·본문 검색")
public class CommentController {
    private final CommentService comments;

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 목록·본문 검색", description = "대댓글은 parentId로 연결합니다. 숨김·삭제 댓글은 목록에서 안내 문구로 표시하고 검색에서는 제외합니다.")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(
            @PathVariable("postId") Long postId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success("댓글 조회가 완료되었습니다.",
                comments.getComments(postId, page, size, keyword)));
    }
}
