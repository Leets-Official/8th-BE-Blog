package com.example.demo.post.controller;

import com.example.demo.global.apiPayload.ApiResponse;
import com.example.demo.global.apiPayload.code.GeneralSuccessCode;
import com.example.demo.post.dto.PostReqDTO;
import com.example.demo.post.dto.PostResDTO;
import com.example.demo.post.serivce.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.apiPayload.code.GeneralSuccessCode.SUCCESS_CODE;

@RequiredArgsConstructor
@RestController
public class PostController implements PostControllerDocs {
    private final PostService postService;

    @Override
    @GetMapping("/post/{postId}")
    public ApiResponse<PostResDTO.PostGetResponse> getPost(
            @PathVariable Long postId
    ) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.SUCCESS_CODE,postService.getPost(postId));
    }

    @Override
    @PostMapping("/post")
    public ApiResponse<PostResDTO.PostCreateResponse> createPost(
            @Valid @RequestBody PostReqDTO.PostCreateRequest Request) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.SUCCESS_CODE, postService.createPost(Request));
    }

    @Override
    @PutMapping("/post/{postId}")
    public ApiResponse<PostResDTO.PostUpdateResponse> updatePost(
            @PathVariable Long postId,@Valid @RequestBody PostReqDTO.PostUpdateRequest Request){
        return ApiResponse.onSuccess(
                GeneralSuccessCode.SUCCESS_CODE, postService.updatePost(postId,Request));
    }

    @Override
    @DeleteMapping("/post/{postId}")
    public ApiResponse<PostResDTO.PostDeleteResponse> deletePost(
            @PathVariable Long postId,@RequestBody PostReqDTO.PostDeletePost Request)
    {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.SUCCESS_CODE,postService.deletePost(postId,Request));
    }


    @Override
    @GetMapping("/post/list")
    public ApiResponse<PostResDTO.PostGetListResponse> getPostList()
    {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.SUCCESS_CODE,postService.getPostList());
    }
}
