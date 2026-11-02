package com.leets.mission.controller;

import com.leets.mission.dto.comment.CommentCreateRequest;
import com.leets.mission.dto.comment.CommentResponse;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import com.leets.mission.global.exception.ErrorResponse;
import com.leets.mission.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "댓글 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = CommentResponse.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "I게시글 ID 형식 오류 (INVALID_POST_ID)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글을 찾을 수 없음 (POST_NOT_FOUND)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<CommentResponse>> getComments(
            @Parameter(description = "조회할 게시글 ID", example = "1")
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    @Operation(
            summary = "댓글 상세 조회",
            description = "게시글 ID와 댓글 ID로 해당 댓글의 상세 정보를 조회합니다."
    )
    @GetMapping("/{commentId}")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "댓글 상세 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 경로 ID 형식 (postId는 INVALID_POST_ID, commentId는 INVALID_INPUT_VALUE)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 또는 댓글을 찾을 수 없음 (POST_NOT_FOUND, COMMENT_NOT_FOUND)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<CommentResponse> getComment(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,

            @Parameter(description = "댓글 ID", example = "2")
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(commentService.getComment(postId, commentId));
    }

    @Operation(
            summary = "댓글 작성",
            description = "요청 회원이 게시글에 댓글을 작성합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "댓글 작성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 또는 게시글 ID 형식 오류 (INVALID_INPUT_VALUE, INVALID_POST_ID)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요함 (UNAUTHORIZED)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글을 찾을 수 없음 (POST_NOT_FOUND)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
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