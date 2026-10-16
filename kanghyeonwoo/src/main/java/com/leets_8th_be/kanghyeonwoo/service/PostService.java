package com.leets_8th_be.kanghyeonwoo.service;

import com.leets_8th_be.kanghyeonwoo.dto.PostCreateRequest;
import com.leets_8th_be.kanghyeonwoo.dto.PostResponse;
import com.leets_8th_be.kanghyeonwoo.entity.Post;
import com.leets_8th_be.kanghyeonwoo.entity.User;
import com.leets_8th_be.kanghyeonwoo.exception.CustomException;
import com.leets_8th_be.kanghyeonwoo.exception.ErrorCode;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = Post.create(request.getTitle(), request.getContent(), author);
        Post savedPost = postRepository.save(post);
        return PostResponse.from(savedPost);
    }

    public com.leets_8th_be.kanghyeonwoo.dto.PostDetailResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return com.leets_8th_be.kanghyeonwoo.dto.PostDetailResponse.from(post);
    }
}
