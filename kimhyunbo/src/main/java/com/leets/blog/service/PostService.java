package com.leets.blog.service;

import com.leets.blog.dto.post.PostCreateRequest;
import com.leets.blog.dto.post.PostDeleteResponse;
import com.leets.blog.dto.post.PostDetailResponse;
import com.leets.blog.dto.post.PostListResponse;
import com.leets.blog.dto.post.PostUpdateRequest;
import com.leets.blog.entity.Member;
import com.leets.blog.entity.Post;
import com.leets.blog.entity.PostStatus;
import com.leets.blog.global.exception.CustomException;
import com.leets.blog.global.exception.ErrorCode;
import com.leets.blog.repository.MemberRepository;
import com.leets.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public List<PostListResponse> getPosts(int page, int size, String keyword) {
        // API는 1페이지부터, Spring Data는 0페이지부터 시작한다.
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

        // 숨김 처리된 게시글은 목록에 보여주지 않는다.
        Page<Post> posts = StringUtils.hasText(keyword)
                ? postRepository.findByStatusAndTitleContaining(PostStatus.ACTIVE, keyword, pageable)
                : postRepository.findByStatus(PostStatus.ACTIVE, pageable);

        return posts.map(PostListResponse::from).toList();
    }

    public PostDetailResponse getPost(Long postId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return PostDetailResponse.from(post);
    }

    @Transactional
    public PostDetailResponse createPost(PostCreateRequest request) {
        Member author = memberRepository.findById(request.authorId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = Post.builder()
                .member(author)
                .title(request.title())
                .content(request.content())
                .build();
        postRepository.saveAndFlush(post);

        return PostDetailResponse.from(post);
    }

    @Transactional
    public PostDetailResponse updatePost(Long postId, PostUpdateRequest request) {
        Post post = findPost(postId);
        validateAuthor(post, request.authorId());

        post.update(request.title(), request.content());
        postRepository.flush();

        return PostDetailResponse.from(post);
    }

    @Transactional
    public PostDeleteResponse deletePost(Long postId, Long authorId) {
        Post post = findPost(postId);
        validateAuthor(post, authorId);

        postRepository.delete(post);
        return new PostDeleteResponse(postId);
    }

    @Transactional
    public PostDetailResponse hidePost(Long postId, Long authorId) {
        Post post = findPost(postId);
        validateAuthor(post, authorId);

        post.hide();
        postRepository.flush();

        return PostDetailResponse.from(post);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void validateAuthor(Post post, Long authorId) {
        if (!post.getMember().getId().equals(authorId)) {
            throw new CustomException(ErrorCode.POST_FORBIDDEN);
        }
    }
}
