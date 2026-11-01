package com.leets.blog.service;

import com.leets.blog.dto.post.*;
import com.leets.blog.entity.Post;
import com.leets.blog.entity.PostStatus;
import com.leets.blog.entity.User;
import com.leets.blog.exception.PostApiException;
import com.leets.blog.repository.PostRepository;
import com.leets.blog.repository.UserRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("mysql")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostListResponse list(Long userId) {
        if (userId != null) validateUserId(userId);
        return new PostListResponse(postRepository.findVisiblePosts(userId).stream()
                .map(PostResponse::from).toList());
    }

    public PostResponse detail(Long postId, Long userId) {
        if (userId != null) validateUserId(userId);
        Post post = findActivePost(postId);
        if (post.getStatus() == PostStatus.HIDDEN) {
            throw new PostApiException(403, "POST_HIDDEN", "숨김 처리된 게시글입니다.");
        }
        if (post.isPrivate() && !Objects.equals(post.getUser().getId(), userId)) {
            throw new PostApiException(403, "POST_ACCESS_DENIED", "해당 게시글에 접근할 권한이 없습니다.");
        }
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse create(CreatePostRequest request) {
        if (request.title() == null || request.title().isBlank()
                || request.content() == null || request.content().isBlank()) {
            throw new PostApiException(400, "INVALID_INPUT", "제목과 내용은 필수 입력 항목입니다.");
        }
        if (request.title().length() > 50) {
            throw new PostApiException(400, "TITLE_TOO_LONG", "게시글 제목은 50자를 초과할 수 없습니다.");
        }
        validateUserId(request.userId());
        User user = userRepository.findByIdAndDeletedAtIsNull(request.userId())
                .orElseThrow(() -> new PostApiException(404, "MEMBER_NOT_FOUND", "존재하지 않는 사용자입니다."));
        Post post = new Post(user, request.title(), request.content(),
                Boolean.TRUE.equals(request.isPrivate()));
        return PostResponse.from(postRepository.saveAndFlush(post));
    }

    @Transactional
    public UpdatePostResponse update(Long postId, UpdatePostRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new PostApiException(400, "TITLE_REQUIRED", "게시글 제목은 필수 입력 항목입니다.");
        }
        validateUserId(request.userId());
        Post post = findActivePost(postId);
        checkOwner(post, request.userId(), "수정");
        post.update(request.title(), request.content());
        postRepository.flush();
        return new UpdatePostResponse(post.getId(), post.getTitle(), post.getContent());
    }

    @Transactional
    public void delete(Long postId, Long userId) {
        validateUserId(userId);
        Post post = findActivePost(postId);
        checkOwner(post, userId, "삭제");
        post.markDeleted();
    }

    @Transactional
    public PostStatusResponse hide(Long postId) {
        if (postId == null || postId <= 0) {
            throw new PostApiException(400, "INVALID_POST_ID", "올바르지 않은 게시글 ID입니다.");
        }
        Post post = postRepository.findForUpdateById(postId)
                .orElseThrow(() -> new PostApiException(404, "POST_NOT_FOUND", "존재하지 않는 게시글입니다."));
        if (post.getDeletedAt() != null) {
            throw new PostApiException(404, "POST_DELETED", "삭제된 게시글입니다.");
        }
        if (post.getStatus() == PostStatus.HIDDEN) {
            throw new PostApiException(409, "POST_ALREADY_HIDDEN", "이미 숨김 처리된 게시글입니다.");
        }
        post.hide();
        postRepository.flush();
        return new PostStatusResponse(post.getId(), post.getStatus());
    }

    private Post findActivePost(Long postId) {
        if (postId == null || postId <= 0) {
            throw new PostApiException(400, "INVALID_POST_ID", "올바르지 않은 게시글 ID입니다.");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostApiException(404, "POST_NOT_FOUND", "존재하지 않는 게시글입니다."));
        if (post.getDeletedAt() != null) {
            throw new PostApiException(404, "POST_DELETED", "삭제된 게시글입니다.");
        }
        return post;
    }

    private void checkOwner(Post post, Long userId, String action) {
        if (!Objects.equals(post.getUser().getId(), userId)) {
            throw new PostApiException(403, "POST_ACCESS_DENIED", "본인이 작성한 게시글만 " + action + "할 수 있습니다.");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new PostApiException(400, "INVALID_INPUT", "올바른 사용자 ID를 입력해주세요.");
        }
    }
}
