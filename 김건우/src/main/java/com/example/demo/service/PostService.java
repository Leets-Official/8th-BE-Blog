package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.User;
import com.example.demo.global.CustomException;
import com.example.demo.global.ErrorCode;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 숨김(HIDDEN) 글은 목록에서 제외
    public PostListResponse getPosts(int page, int size) {
        Page<Post> result = postRepository.findAllByStatus(PostStatus.ACTIVE,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        return PostListResponse.from(result);
    }

    // 숨김(HIDDEN) 글은 상세 조회도 404
    public PostResponse getPost(Long postId) {
        Post post = findPost(postId);
        if (post.isHidden()) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse createPost(PostRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = Post.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .build();

        return PostResponse.from(postRepository.save(post));
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostRequest request) {
        Post post = findPost(postId);
        validateWriter(post, request.userId());

        post.update(request.title(), request.content());
        postRepository.flush(); // updatedAt 이 갱신된 뒤 응답을 만들기 위해
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse patchPost(Long postId, PostPatchRequest request) {
        if (request.title() == null && request.content() == null) {
            throw new CustomException(ErrorCode.NO_FIELD_TO_UPDATE);
        }

        Post post = findPost(postId);
        validateWriter(post, request.userId());

        post.update(
                request.title() != null ? request.title() : post.getTitle(),
                request.content() != null ? request.content() : post.getContent());
        postRepository.flush();
        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = findPost(postId);
        validateWriter(post, userId);
        postRepository.delete(post); // cascade = REMOVE 로 댓글도 함께 삭제
    }

    // [4주차] 게시글 숨김 (ACTIVE -> HIDDEN)
    @Transactional
    public PostHideResponse hidePost(Long postId, Long userId) {
        // 행 잠금: 동시에 숨김 요청이 와도 상태 검사가 한 번에 하나씩 수행됨
        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        validateWriter(post, userId);

        post.hide();
        return PostHideResponse.from(post);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void validateWriter(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_POST);
        }
    }
}
