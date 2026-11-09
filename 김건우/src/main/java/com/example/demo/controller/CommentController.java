package com.example.demo.controller;

import com.example.demo.dto.CommentRequest;
import com.example.demo.dto.CommentResponse;
import com.example.demo.global.ApiResponse;
import com.example.demo.global.ErrorResponse;
import com.example.demo.global.SuccessCode;
import com.example.demo.global.SwaggerErrorExamples;
import com.example.demo.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment", description = "댓글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    // @ResponseStatus 는 문서용 (실제 상태 코드는 ApiResponse.success 가 결정)
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다. 숨김 처리된 게시글에는 작성할 수 없습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMON_400_1", value = SwaggerErrorExamples.INVALID_BODY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 또는 사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "POST_404_1", summary = "게시글 없음 (숨김 포함)", value = SwaggerErrorExamples.POST_NOT_FOUND),
                                    @ExampleObject(name = "USER_404_1", summary = "사용자 없음", value = SwaggerErrorExamples.USER_NOT_FOUND)
                            }))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        return ApiResponse.success(SuccessCode.COMMENT_CREATED, commentService.createComment(postId, request));
    }

    // 댓글 채택: 액션형
    @Operation(summary = "댓글 채택",
            description = "게시글 작성자가 댓글 하나를 채택합니다. 게시글당 채택은 1개만 가능하며, "
                    + "작성자 본인의 댓글은 채택할 수 없습니다. 이미 채택된 댓글이 있으면 다른 댓글을 채택할 수 없습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "userId 누락 또는 본인 댓글 채택",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "COMMON_400_1", summary = "userId 누락", value = SwaggerErrorExamples.INVALID_PARAM),
                                    @ExampleObject(name = "COMMENT_400_1", summary = "본인 댓글 채택", value = SwaggerErrorExamples.SELF_ACCEPT_NOT_ALLOWED)
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "게시글 작성자가 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMENT_403_1", value = SwaggerErrorExamples.NOT_POST_OWNER))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 또는 댓글 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "POST_404_1", summary = "게시글 없음 (숨김 포함)", value = SwaggerErrorExamples.POST_NOT_FOUND),
                                    @ExampleObject(name = "COMMENT_404_1", summary = "댓글 없음 (다른 게시글의 댓글 포함)", value = SwaggerErrorExamples.COMMENT_NOT_FOUND)
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 채택된 댓글이거나, 게시글에 채택된 댓글이 이미 있음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "COMMENT_409_1", summary = "이미 채택된 댓글", value = SwaggerErrorExamples.COMMENT_ALREADY_ACCEPTED),
                                    @ExampleObject(name = "COMMENT_409_2", summary = "다른 댓글이 이미 채택됨", value = SwaggerErrorExamples.ANOTHER_COMMENT_ACCEPTED)
                            }))
    })
    @PostMapping("/{commentId}/accept")
    public ResponseEntity<ApiResponse<CommentResponse>> acceptComment(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
            @Parameter(description = "채택할 댓글 ID", example = "1") @PathVariable Long commentId,
            @Parameter(description = "요청자 ID (게시글 작성자와 같아야 함)", example = "1") @RequestParam Long userId) {
        return ApiResponse.success(SuccessCode.COMMENT_ACCEPTED,
                commentService.acceptComment(postId, commentId, userId));
    }
}
