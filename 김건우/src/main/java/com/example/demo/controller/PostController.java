package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.global.ApiResponse;
import com.example.demo.global.ErrorResponse;
import com.example.demo.global.SuccessCode;
import com.example.demo.global.SwaggerErrorExamples;
import com.example.demo.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// 참고: 우리 공통 응답 클래스 이름(ApiResponse)이 Swagger 어노테이션 이름과 같아서,
//       Swagger 쪽은 패키지명을 모두 적어(@io.swagger...responses.ApiResponse) 사용한다.
@Tag(name = "Post", description = "게시글 API")
@Validated
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회",
            description = "최신순으로 게시글을 페이징 조회합니다. 숨김(HIDDEN) 처리된 글은 목록에서 제외됩니다. 본문은 포함하지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "page / size 범위 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMON_400_1", value = SwaggerErrorExamples.INVALID_PAGING)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponse>> getPosts(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page는 0 이상의 숫자여야 합니다.") int page,
            @Parameter(description = "페이지 크기 (1 이상)", example = "10")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size는 1 이상의 숫자여야 합니다.") int size) {
        return ApiResponse.success(SuccessCode.POST_LIST_OK, postService.getPosts(page, size));
    }

    @Operation(summary = "게시글 상세 조회",
            description = "게시글 한 건을 조회합니다. 숨김(HIDDEN) 처리된 글은 존재하지 않는 글로 취급해 404 를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음 (또는 숨김 처리됨)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "POST_404_1", value = SwaggerErrorExamples.POST_NOT_FOUND)))
    })
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId) {
        return ApiResponse.success(SuccessCode.POST_DETAIL_OK, postService.getPost(postId));
    }

    // @ResponseStatus 는 문서용: 실제 상태 코드는 ApiResponse.success 의 ResponseEntity 가 결정한다.
    //                          (Swagger 가 성공 응답 코드를 201 로 표시하도록 알려주는 역할)
    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다. 작성자는 요청 본문의 userId 로 식별합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMON_400_1", value = SwaggerErrorExamples.INVALID_BODY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "USER_404_1", value = SwaggerErrorExamples.USER_NOT_FOUND)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody PostRequest request) {
        return ApiResponse.success(SuccessCode.POST_CREATED, postService.createPost(request));
    }

    @Operation(summary = "게시글 전체 수정",
            description = "제목과 내용을 모두 교체합니다. 글 작성자만 수정할 수 있습니다. 일부만 수정하려면 PATCH 를 사용하세요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMON_400_1", value = SwaggerErrorExamples.INVALID_BODY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "작성자가 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMON_403_1", value = SwaggerErrorExamples.FORBIDDEN_POST))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "POST_404_1", value = SwaggerErrorExamples.POST_NOT_FOUND)))
    })
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
            @Valid @RequestBody PostRequest request) {
        return ApiResponse.success(SuccessCode.POST_UPDATED, postService.updatePost(postId, request));
    }

    @Operation(summary = "게시글 부분 수정",
            description = "title, content 중 보낸 값만 수정합니다. 둘 다 보내지 않으면 400 입니다. 글 작성자만 수정할 수 있습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 또는 수정할 필드 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "COMMON_400_1", summary = "입력값 검증 실패", value = SwaggerErrorExamples.INVALID_BODY),
                                    @ExampleObject(name = "COMMON_400_2", summary = "수정할 필드 없음", value = SwaggerErrorExamples.NO_FIELD_TO_UPDATE)
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "작성자가 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMON_403_1", value = SwaggerErrorExamples.FORBIDDEN_POST))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "POST_404_1", value = SwaggerErrorExamples.POST_NOT_FOUND)))
    })
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> patchPost(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
            @Valid @RequestBody PostPatchRequest request) {
        return ApiResponse.success(SuccessCode.POST_UPDATED, postService.patchPost(postId, request));
    }

    @Operation(summary = "게시글 삭제", description = "게시글과 그 댓글을 함께 삭제합니다. 글 작성자만 삭제할 수 있습니다. 성공 시 data 는 null 입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "userId 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMON_400_1", value = SwaggerErrorExamples.INVALID_PARAM))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "작성자가 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMON_403_1", value = SwaggerErrorExamples.FORBIDDEN_POST))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "POST_404_1", value = SwaggerErrorExamples.POST_NOT_FOUND)))
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
            @Parameter(description = "요청자 ID (글 작성자와 같아야 함)", example = "1") @RequestParam Long userId) {
        postService.deletePost(postId, userId);
        return ApiResponse.success(SuccessCode.POST_DELETED, null);
    }

    // [4주차] 게시글 숨김 (액션형)
    @Operation(summary = "게시글 숨김",
            description = "게시글 상태를 ACTIVE 에서 HIDDEN 으로 변경합니다. 글 작성자만 숨길 수 있고, 이미 숨김 상태면 409 입니다. "
                    + "숨김 처리된 글은 목록, 상세 조회에서 제외되고 신고, 댓글 작성도 할 수 없습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "userId 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMON_400_1", value = SwaggerErrorExamples.INVALID_PARAM))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "작성자가 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMON_403_1", value = SwaggerErrorExamples.FORBIDDEN_POST))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "POST_404_1", value = SwaggerErrorExamples.POST_NOT_FOUND))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 숨김 처리된 게시글",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "POST_409_1", value = SwaggerErrorExamples.POST_ALREADY_HIDDEN)))
    })
    @PostMapping("/{postId}/hide")
    public ResponseEntity<ApiResponse<PostHideResponse>> hidePost(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
            @Parameter(description = "요청자 ID (글 작성자와 같아야 함)", example = "1") @RequestParam Long userId) {
        return ApiResponse.success(SuccessCode.POST_HIDDEN, postService.hidePost(postId, userId));
    }
}
