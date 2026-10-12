package com.leets.blog.service;

import com.leets.blog.dto.post.*;
import com.leets.blog.entity.Post;
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

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new PostApiException(400, "INVALID_INPUT", "올바른 사용자 ID를 입력해주세요.");
        }
    }
}
