package com.leets.choyeji.controller;

// import com.leets.choyeji.auth.JwtMemberResolver;
import com.leets.choyeji.dto.ApiResponse;
import com.leets.choyeji.dto.PostListResponse;
import com.leets.choyeji.dto.PostRequest;
import com.leets.choyeji.dto.PostResponse;
import com.leets.choyeji.dto.PostUpdateResponse;
import com.leets.choyeji.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    // private final JwtMemberResolver jwtMemberResolver;

    @Operation(summary = "게시글 조회")
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> get(@PathVariable Long postId) {
        return ApiResponse.success(postService.get(postId));
    }

    @Operation(summary = "게시글 목록 조회")
    @GetMapping
    public ApiResponse<PostListResponse> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(postService.list(page, size));
    }

    @Operation(summary = "게시글 생성")
    @PostMapping
    public ApiResponse<PostResponse> create(
            // @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody PostRequest request) {
        // return ApiResponse.success(postService.create(jwtMemberResolver.resolve(authorization), request));
        return ApiResponse.success(postService.create(request));
    }

    @Operation(summary = "게시글 수정")
    @PatchMapping("/{postId}")
    public ApiResponse<PostUpdateResponse> update(@PathVariable Long postId,
            // @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody PostRequest request) {
        // return ApiResponse.success(postService.update(jwtMemberResolver.resolve(authorization), postId, request));
        return ApiResponse.success(postService.update(postId, request));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(
            // @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long postId) {
        // postService.delete(jwtMemberResolver.resolve(authorization), postId);
        postService.delete(postId);
        return ApiResponse.success(null);
    }
}
