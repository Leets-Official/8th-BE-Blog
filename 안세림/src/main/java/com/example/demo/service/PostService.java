package com.example.demo.service;

import com.example.demo.converter.PostConverter;
import com.example.demo.dto.post.PostCreateRequest;
import com.example.demo.dto.post.PostDeleteResponse;
import com.example.demo.dto.post.PostDetailResponse;
import com.example.demo.dto.post.PostHideRequest;
import com.example.demo.dto.post.PostListResponse;
import com.example.demo.dto.post.PostStatusResponse;
import com.example.demo.dto.post.PostUpdateRequest;
import com.example.demo.entity.Member;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;

    public List<PostListResponse> getPosts(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = (keyword == null || keyword.isBlank())
                ? postRepository.findByStatus(PostStatus.ACTIVE, pageable)
                : postRepository.findByStatusAndTitleContaining(PostStatus.ACTIVE, keyword, pageable);
        return posts.stream()
                .map(PostConverter::toListResponse)
                .toList();
    }

    public PostDetailResponse getPost(Long postId) {
        return PostConverter.toDetailResponse(findActivePost(postId));
    }

    @Transactional
    public PostDetailResponse createPost(PostCreateRequest request) {
        Member member = memberRepository.findById(request.authorId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Post post = postRepository.save(new Post(request.title(), request.content(), member));
        return PostConverter.toDetailResponse(post);
    }

    @Transactional
    public PostDetailResponse updatePost(Long postId, PostUpdateRequest request) {
        Post post = findPost(postId);
        checkAuthor(post, request.authorId());
        post.update(request.title(), request.content());
        postRepository.flush();
        return PostConverter.toDetailResponse(post);
    }

    @Transactional
    public PostStatusResponse hidePost(Long postId, PostHideRequest request) {
        Post post = findPost(postId);
        checkAuthor(post, request.authorId());
        post.hide();
        return new PostStatusResponse(post.getId(), post.getStatus());
    }

    @Transactional
    public PostDeleteResponse deletePost(Long postId, Long authorId) {
        Post post = findPost(postId);
        checkAuthor(post, authorId);
        reportRepository.deleteByCommentPost(post);
        reportRepository.deleteByPost(post);
        postRepository.delete(post);
        return new PostDeleteResponse(postId);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private Post findActivePost(Long postId) {
        Post post = findPost(postId);
        if (post.isHidden()) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        return post;
    }

    private void checkAuthor(Post post, Long authorId) {
        if (!post.getMember().getId().equals(authorId)) {
            throw new CustomException(ErrorCode.POST_FORBIDDEN);
        }
    }
}