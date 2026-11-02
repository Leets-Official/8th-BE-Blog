package com.leets.mission.domain.post.controller;

import com.leets.mission.domain.post.dto.PostRequestDTO;
import com.leets.mission.domain.post.dto.PostResponseDTO;
import com.leets.mission.domain.post.service.PostService;
import com.leets.mission.global.apiPayload.ApiResponse;
import com.leets.mission.global.apiPayload.code.SuccessStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.leets.mission.global.apiPayload.ErrorResponseSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "게시글", description = "게시글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @Operation(summary = "게시글 생성")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "생성 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: 입력값 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "USER_404_1: 사용자 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponseDTO.DetailDTO> createPost(@RequestBody @Valid PostRequestDTO.CreateDTO request) {
        return ApiResponse.of(SuccessStatus.POST_CREATED, postService.createPost(request));
    }

    // 게시글 목록 조회
    @Operation(summary = "게시글 목록 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: page, size 형식 오류 / POST_400_2: page는 0 이상, size는 1 이상이어야 함",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @GetMapping
    public ApiResponse<PostResponseDTO.PreviewListDTO> getPostList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.onSuccess(postService.getPostList(page, size));
    }

    // 게시글 단일 조회
    @Operation(summary = "게시글 단일 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: postId 형식 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "POST_404_1: 게시글 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @GetMapping("/{postId}")
    public ApiResponse<PostResponseDTO.DetailDTO> getPost(@PathVariable Long postId) {
        return ApiResponse.onSuccess(postService.getPost(postId));
    }

    // 게시글 전체 수정
    @Operation(summary = "게시글 전체 수정")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "수정 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: 입력값 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "POST_403_1: 본인 글만 수정 가능",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "POST_404_1: 게시글 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @PutMapping("/{postId}")
    public ApiResponse<PostResponseDTO.DetailDTO> updatePost(
            @PathVariable Long postId,  //url에
            @RequestBody @Valid PostRequestDTO.UpdateDTO request //본문에
    ) {
        return ApiResponse.of(SuccessStatus.POST_UPDATED, postService.updatePost(postId, request));
    }

    // 게시글 부분 수정
    @Operation(summary = "게시글 부분 수정")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "수정 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: 입력값 오류 / POST_400_1: 수정할 제목 또는 본문 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "POST_403_1: 본인 글만 수정 가능",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "POST_404_1: 게시글 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @PatchMapping("/{postId}")
    public ApiResponse<PostResponseDTO.DetailDTO> patchPost(
            @PathVariable Long postId,
            @RequestBody @Valid PostRequestDTO.PatchDTO request
    ) {
        return ApiResponse.of(SuccessStatus.POST_UPDATED, postService.patchPost(postId, request));
    }

    // 게시글 숨김 (userId -> 쿼리 파라미터로)
    @Operation(summary = "게시글 숨김")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "숨김 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: 형식 오류 / COMMON_400_2: userId 누락",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "POST_403_3: 본인 글만 숨김 가능",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "POST_404_1: 게시글 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "POST_409_1: 이미 숨김 처리됨",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @PatchMapping("/{postId}/hide")
    public ApiResponse<PostResponseDTO.StatusDTO> hidePost(
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        return ApiResponse.of(SuccessStatus.POST_HIDDEN, postService.hidePost(postId, userId));
    }

    // 게시글 삭제 (userId -> 쿼리 파라미터로)
    @Operation(summary = "게시글 삭제")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "삭제 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "COMMON_400_1: 형식 오류 / COMMON_400_2: userId 누락",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "POST_403_2: 본인 글만 삭제 가능",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "POST_404_1: 게시글 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
    @DeleteMapping("/{postId}")
    public ApiResponse<PostResponseDTO.DeleteDTO> deletePost(
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        return ApiResponse.of(SuccessStatus.POST_DELETED, postService.deletePost(postId, userId));
    }
}
