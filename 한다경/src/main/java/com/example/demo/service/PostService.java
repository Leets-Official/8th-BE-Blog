package com.example.demo.service;

import com.example.demo.domain.Member;
import com.example.demo.domain.Post;
import com.example.demo.dto.request.PostCreateRequest;
import com.example.demo.dto.request.PostUpdateRequest;
import com.example.demo.dto.response.PostListResponse;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public PostService(PostRepository postRepository, MemberRepository memberRepository) {
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
    }

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
        validatePostId(postId);

        Post post = findPostById(postId);
        validateReadablePost(post, userId);

        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse createPost(Long userId, PostCreateRequest request) {
        Member member = findMemberById(userId);
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
        validatePostId(postId);

        Post post = findPostById(postId);
        validateActivePost(post);
        validatePostOwner(post, userId);

        post.update(request.title(), request.content());
        postRepository.flush();

        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        validatePostId(postId);

        Post post = findPostById(postId);
        validateActivePost(post);
        validatePostOwner(post, userId);

        post.softDelete();
    }

    private Member findMemberById(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return memberRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_POST_ID);
        }
    }

    private void validateReadablePost(Post post, Long userId) {
        if (post.isDeleted()) {
            throw new BusinessException(ErrorCode.POST_DELETED);
        }

        if (post.isHidden()) {
            throw new BusinessException(ErrorCode.POST_ACCESS_DENIED);
        }

        if (post.isPrivate() && post.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.POST_ACCESS_DENIED);
        }
    }

    private void validateActivePost(Post post) {
        if (post.isDeleted()) {
            throw new BusinessException(ErrorCode.POST_DELETED);
        }
    }

    private void validatePostOwner(Post post, Long userId) {
        if (post.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.POST_ACCESS_DENIED);
        }
    }
}
