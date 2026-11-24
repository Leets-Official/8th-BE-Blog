package com.example.demo.service;

import com.example.demo.domain.Member;
import com.example.demo.domain.Post;
import com.example.demo.dto.request.PostCreateRequest;
import com.example.demo.dto.request.PostUpdateRequest;
import com.example.demo.dto.response.PostListResponse;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final InteractionAccess access;

    @Transactional(readOnly = true)
    public PostListResponse findPosts() {
        return new PostListResponse(
                postRepository.findAllByDeletedAtIsNullAndIsPrivateFalseAndIsHiddenFalseOrderByCreatedAtDesc()
                        .stream()
                        .map(PostResponse::from)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public PostResponse findPost(Long postId, Long userId) {
        return PostResponse.from(access.readablePost(postId, userId, ErrorCode.INVALID_POST_ID));
    }

    @Transactional
    public PostResponse createPost(Long userId, PostCreateRequest request) {
        Member member = access.activeMember(userId);
        Post post = new Post(
                request.title(),
                request.content(),
                member,
                request.privateValue()
        );

        return PostResponse.from(postRepository.saveAndFlush(post));
    }

    @Transactional
    public PostResponse updatePost(Long postId, Long userId, PostUpdateRequest request) {
        Post post = access.ownedPost(postId, userId);

        post.update(request.title(), request.content());
        postRepository.flush();

        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        access.ownedPost(postId, userId).softDelete();
    }
}
