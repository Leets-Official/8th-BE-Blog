package com.example.demo.service;

import com.example.demo.converter.PostConverter;
import com.example.demo.dto.post.PostCreateRequest;
import com.example.demo.dto.post.PostDeleteResponse;
import com.example.demo.dto.post.PostDetailResponse;
import com.example.demo.dto.post.PostListResponse;
import com.example.demo.dto.post.PostUpdateRequest;
import com.example.demo.entity.Member;
import com.example.demo.entity.Post;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.PostRepository;
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

    public List<PostListResponse> getPosts(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = (keyword == null || keyword.isBlank())
                ? postRepository.findAll(pageable)
                : postRepository.findByTitleContaining(keyword, pageable);
        return posts.stream()
                .map(PostConverter::toListResponse)
                .toList();
    }

    public PostDetailResponse getPost(Long postId) {
        return PostConverter.toDetailResponse(findPost(postId));
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
    public PostDeleteResponse deletePost(Long postId, Long authorId) {
        Post post = findPost(postId);
        checkAuthor(post, authorId);
        postRepository.delete(post);
        return new PostDeleteResponse(postId);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void checkAuthor(Post post, Long authorId) {
        if (!post.getMember().getId().equals(authorId)) {
            throw new CustomException(ErrorCode.POST_FORBIDDEN);
        }
    }
}