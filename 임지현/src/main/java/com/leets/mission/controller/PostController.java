package com.leets.mission.controller;

import com.leets.mission.dto.post.PostCreateRequest;
import com.leets.mission.dto.post.PostListResponse;
import com.leets.mission.dto.post.PostResponse;
import com.leets.mission.dto.post.PostUpdateRequest;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import com.leets.mission.global.exception.ErrorResponse;
import com.leets.mission.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;

@Tag(name = "게시글", description = "게시글 목록/상세 조회, 작성/수정/삭제 API")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private static final Set<String> ALLOWED_SORT_TYPES =
            Set.of("latest", "oldest", "popular");

    @Operation(
            summary = "게시글 목록 조회",
            description = "페이지 번호, 페이지 크기, 정렬 기준으로 게시글 목록을 조회합니다."
    )
    @GetMapping
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostListResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "페이지 번호가 올바르지 않습니다. (INVALID_PAGE_NUMBER), "
                            + "페이지 크기는 1~50 사이여야 합니다. (INVALID_PAGE_SIZE), "
                            + "지원하지 않는 정렬 기준입니다. (INVALID_SORT_TYPE)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<PostListResponse> getPosts(
            @Parameter(
                    description = "페이지 번호, 1부터 시작",
                    example = "1"
            )
            @RequestParam(defaultValue = "1") int page,

            @Parameter(
                    description = "페이지 크기 (1~50)",
                    example = "10"
            )
            @RequestParam(defaultValue = "10") int size,

            @Parameter(
                    description = "정렬 기준: latest, oldest, popular",
                    example = "latest"
            )
            @RequestParam(defaultValue = "latest") String sort
    ) {
        if (page < 1) {
            throw new CustomException(ErrorCode.INVALID_PAGE_NUMBER);
        }
        if (size < 1 || size > 50) {
            throw new CustomException(ErrorCode.INVALID_PAGE_SIZE);
        }
        if (!ALLOWED_SORT_TYPES.contains(sort)) {
            throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
        }

        Sort sortCondition = switch (sort) {
            case "oldest" -> Sort.by("createdAt").ascending();
            case "popular" -> Sort.by("viewCount").descending();
            default -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(page - 1, size, sortCondition);

        return ResponseEntity.ok(postService.getPosts(pageable));
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글 ID로 해당 게시글의 상세 정보를 조회합니다."
    )
    @GetMapping("/{postId}")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시글 ID 형식이 올바르지 않습니다. (INVALID_POST_ID)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 게시글을 찾을 수 없습니다. (POST_NOT_FOUND)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<PostResponse> getPost(
            @Parameter(description = "조회할 게시글 ID", example = "1")
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @Operation(
            summary = "게시글 작성",
            description = "요청 회원이 새 게시글을 작성합니다."
    )
    @PostMapping
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "게시글 작성 성공. 응답 본문은 없으며 Location 헤더에 생성된 게시글 URL을 반환합니다.",
                    headers = @Header(
                            name = "Location",
                            description = "생성된 게시글 URL",
                            schema = @Schema(type = "string", example = "/posts/123")
                    ),
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 입력값입니다. (INVALID_INPUT_VALUE)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요합니다. (UNAUTHORIZED)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "잠시 후 다시 시도해 주세요. (DUPLICATE_POST_SUBMISSION)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<Void> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @Parameter(
                    description = "요청 회원 ID",
                    example = "1",
                    required = true
            )
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Long createdPostId = postService.createPost(request, memberId);
        URI location = URI.create("/posts/" + createdPostId);

        return ResponseEntity.created(location).build();
    }

    @Operation(
            summary = "게시글 수정",
            description = "요청 회원이 작성한 게시글의 제목과 본문을 수정합니다."
    )
    @PatchMapping("/{postId}")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시글 ID 형식이 올바르지 않습니다. (INVALID_POST_ID), "
                            + "유효하지 않은 입력값입니다. (INVALID_INPUT_VALUE)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요합니다. (UNAUTHORIZED)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인이 작성한 게시글만 수정 및 삭제할 수 있습니다. (POST_ACCESS_DENIED)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 게시글을 찾을 수 없습니다. (POST_NOT_FOUND)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "수정할 게시글 ID", example = "1")
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request,
            @Parameter(
                    description = "요청 회원 ID",
                    example = "1",
                    required = true
            )
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        PostResponse response = postService.updatePost(postId, request, memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "게시글 삭제",
            description = "요청 회원이 작성한 게시글을 삭제 처리합니다."
    )
    @DeleteMapping("/{postId}")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "게시글 삭제 성공. 응답 본문은 없습니다.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시글 ID 형식이 올바르지 않습니다. (INVALID_POST_ID)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요합니다. (UNAUTHORIZED)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인이 작성한 게시글만 수정 및 삭제할 수 있습니다. (POST_ACCESS_DENIED)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 게시글을 찾을 수 없습니다. (POST_NOT_FOUND)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "삭제할 게시글 ID", example = "1")
            @PathVariable Long postId,
            @Parameter(
                    description = "요청 회원 ID",
                    example = "1",
                    required = true
            )
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        postService.deletePost(postId, memberId);
        return ResponseEntity.noContent().build();
    }
}