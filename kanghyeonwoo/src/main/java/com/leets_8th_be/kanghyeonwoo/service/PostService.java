package com.leets_8th_be.kanghyeonwoo.service;

import com.leets_8th_be.kanghyeonwoo.dto.PostCreateRequest;
import com.leets_8th_be.kanghyeonwoo.dto.PostDetailResponse;
import com.leets_8th_be.kanghyeonwoo.dto.PostResponse;
import com.leets_8th_be.kanghyeonwoo.dto.PostSummaryResponse;
import com.leets_8th_be.kanghyeonwoo.entity.Post;
import com.leets_8th_be.kanghyeonwoo.entity.User;
import com.leets_8th_be.kanghyeonwoo.exception.CustomException;
import com.leets_8th_be.kanghyeonwoo.exception.ErrorCode;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = Post.create(request.getTitle(), request.getContent(), author);
        Post savedPost = postRepository.save(post);
        return PostResponse.from(savedPost);
    }

    public PostDetailResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return PostDetailResponse.from(post);
    }

    public List<PostSummaryResponse> getPosts(int page, int size, String keyword) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));

        Page<Post> postPage;
        if (keyword != null && !keyword.isBlank()) {
            postPage = postRepository.findByTitleContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            postPage = postRepository.findAll(pageable);
        }

        return postPage.getContent().stream()
                .map(PostSummaryResponse::from)
                .toList();
    }

    @Transactional
    public PostDetailResponse updatePost(Long postId, com.leets_8th_be.kanghyeonwoo.dto.PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(request.getAuthorId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_POST_ACCESS);
        }

        post.update(request.getTitle(), request.getContent());
        return PostDetailResponse.from(post);
    }
}
