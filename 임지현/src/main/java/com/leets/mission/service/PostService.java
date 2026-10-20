package com.leets.mission.service;

import com.leets.mission.domain.member.Member;
import com.leets.mission.domain.member.MemberRepository;
import com.leets.mission.domain.post.Post;
import com.leets.mission.domain.post.PostRepository;
import com.leets.mission.dto.post.PostCreateRequest;
import com.leets.mission.dto.post.PostListResponse;
import com.leets.mission.dto.post.PostResponse;
import com.leets.mission.dto.post.PostUpdateRequest;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public PostListResponse getPosts(Pageable pageable) {
        Page<PostResponse> posts = postRepository.findAllByDeletedAtIsNull(pageable)
                .map(PostResponse::from);
        return PostListResponse.of(posts);
    }

    @Transactional
    public PostResponse getPost(Long postId) {

        int updatedCount = postRepository.increaseViewCount(postId);
        if (updatedCount == 0) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        return PostResponse.from(post);
    }

    @Transactional
    public Long createPost(PostCreateRequest request, Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        LocalDateTime threeSecondsAgo = LocalDateTime.now().minusSeconds(3);
        boolean existsRecentPost = postRepository.existsByMemberIdAndTitleAndCreatedAtAfterAndDeletedAtIsNull(
                memberId,
                request.title(),
                threeSecondsAgo
        );

        if (existsRecentPost) {
            throw new CustomException(ErrorCode.DUPLICATE_POST_SUBMISSION);
        }

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .member(member)
                .build();

        return postRepository.save(post).getId();
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, Long currentMemberId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        validateAuthor(post, currentMemberId);

        post.update(request.title(), request.content());
        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId, Long currentMemberId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        validateAuthor(post, currentMemberId);

        post.delete();
    }

    private void validateAuthor(Post post, Long currentMemberId) {
        if (currentMemberId == null || post.getMember() == null || !post.getMember().getId().equals(currentMemberId)) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }
    }
}