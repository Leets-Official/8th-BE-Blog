package com.leets.blog.domain.post.service;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.comment.repository.CommentRepository;
import com.leets.blog.domain.member.entity.Member;
import com.leets.blog.domain.member.repository.MemberRepository;
import com.leets.blog.domain.post.entity.Post;
import com.leets.blog.domain.post.repository.PostRepository;
import com.leets.blog.domain.post.web.dto.request.PostCreateRequest;
import com.leets.blog.domain.post.web.dto.request.PostUpdateRequest;
import com.leets.blog.domain.post.web.dto.request.PostSortType;
import com.leets.blog.domain.post.web.dto.response.PostDeleteResponse;
import com.leets.blog.domain.post.web.dto.response.PostDetailResponse;
import com.leets.blog.domain.post.web.dto.response.PostListResponse;
import com.leets.blog.domain.post.web.dto.response.PostResponse;
import com.leets.blog.domain.post.web.dto.response.PostSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 50;

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    // 게시글 목록 조회
    public PostListResponse getPosts(int page, int size, String sort) {
        validatePage(page, size);
        PostSortType sortType = PostSortType.from(sort);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PostSummaryResponse> result = switch (sortType) {
            case LATEST -> postRepository.findLatest(pageable);
            case OLDEST -> postRepository.findOldest(pageable);
            case POPULAR -> postRepository.findPopular(pageable);
        };
        return PostListResponse.from(result);
    }

    // 게시글 상세 조회
    public PostDetailResponse getPost(Long postId) {
        return PostDetailResponse.from(findPost(postId));
    }

    // 게시글 생성
    @Transactional
    public PostResponse createPost(Long memberId, PostCreateRequest request) {
        Member member = findMember(memberId);
        Post post = postRepository.save(Post.builder()
                .member(member)
                .title(request.title())
                .content(request.content())
                .build());
        return PostResponse.from(post);
    }

    // 게시글 수정
    @Transactional
    public PostResponse updatePost(Long memberId, Long postId, PostUpdateRequest request) {
        Post post = findWritablePost(memberId, postId);
        post.update(request.title(), request.content());
        return PostResponse.from(post);
    }

    // 게시글 삭제 (soft delete), 그 글의 댓글도 함께 삭제
    @Transactional
    public PostDeleteResponse deletePost(Long memberId, Long postId) {
        Post post = findWritablePost(memberId, postId);
        post.delete();
        commentRepository.softDeleteAllByPostId(post.getId(), post.getDeletedAt());
        return new PostDeleteResponse(post.getId());
    }

    private void validatePage(int page, int size) {
        if (page < 1) {
            throw new CustomException(ErrorCode.INVALID_PAGE_NUMBER);
        }
        if (size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
            throw new CustomException(ErrorCode.INVALID_PAGE_SIZE);
        }
    }

    // 0 이하 ID는 400, 없거나 삭제된 글은 404
    private Post findPost(Long postId) {
        if (postId < 1) {
            throw new CustomException(ErrorCode.INVALID_POST_ID);
        }
        return postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    // X-Member-Id로 받은 회원 확인, 없거나 탈퇴한 회원이면 401
    private Member findMember(Long memberId) {
        if (memberId < 1) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
    }

    // 수정·삭제 공통: 회원(401) → 게시글 ID(400) → 게시글 존재(404) → 작성자(403) 순으로 확인
    private Post findWritablePost(Long memberId, Long postId) {
        findMember(memberId);
        Post post = findPost(postId);
        if (!post.isWrittenBy(memberId)) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }
        return post;
    }
}
