package com.leets.mission.domain.post.controller;

import com.leets.mission.domain.post.dto.PostRequestDTO;
import com.leets.mission.domain.post.dto.PostResponseDTO;
import com.leets.mission.domain.post.service.PostService;
import com.leets.mission.global.apiPayload.ApiResponse;
import com.leets.mission.global.apiPayload.code.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponseDTO.DetailDTO> createPost(@RequestBody @Valid PostRequestDTO.CreateDTO request) {
        return ApiResponse.of(SuccessStatus.POST_CREATED, postService.createPost(request));
    }

    // 게시글 단일 조회
    @GetMapping("/{postId}")
    public ApiResponse<PostResponseDTO.DetailDTO> getPost(@PathVariable Long postId) {
        return ApiResponse.onSuccess(postService.getPost(postId));
    }
}
