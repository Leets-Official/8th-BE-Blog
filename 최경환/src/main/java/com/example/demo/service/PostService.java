package com.example.demo.service;

import com.example.demo.dto.PostResponse;
import com.example.demo.dto.PostWriteRequest;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SoftDeleteService softDeleteService;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            SoftDeleteService softDeleteService
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.softDeleteService = softDeleteService;
    }

    // 게시글 목록 조회
    @Transactional(readOnly = true)
    public List<PostResponse> findAll(Long userId) {
        return postRepository.findAll().stream()
                .filter(post -> !post.isPrivate() || isOwner(post, userId))
                .map(this::toResponse)
                .toList();
    }

    // 게시물 작성
    public PostResponse create(Long userId, PostWriteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "작성자를 찾을 수 없습니다."));
        Post post = new Post(request.title(), request.content(), request.isPrivate(), user);
        return toResponse(postRepository.save(post));
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public PostResponse findById(Long postId, Long userId) {
        Post post = getPost(postId);
        if (post.isPrivate() && !isOwner(post, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "비공개 게시글을 조회할 권한이 없습니다.");
        }
        return toResponse(post);
    }

    // 게시글 수정
    public PostResponse update(Long postId, Long userId, PostWriteRequest request) {
        Post post = getPost(postId);
        requireOwner(post, userId, "수정");
        post.update(request.title(), request.content(), request.isPrivate());
        return toResponse(post);
    }

    // 게시글 삭제
    public void delete(Long postId, Long userId) {
        softDeleteService.deletePost(postId, userId);
    }

    // 삭제되지 않은 게시글을 조회하고 없으면 404 예외를 발생시킴
    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "게시글이 존재하지 않거나 삭제되었습니다."
                ));
    }

    // 요청 사용자가 작성자인지 확인하고 아니면 접근을 거부
    private void requireOwner(Post post, Long userId, String action) {
        if (!isOwner(post, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글 작성자만 " + action + "할 수 있습니다.");
        }
    }

    // 요청 사용자와 게시글 작성자가 같은지 확인
    private boolean isOwner(Post post, Long userId) {
        return userId != null && post.getUser().getId().equals(userId);
    }

    // 게시글 엔티티를 API 응답 DTO로 변환
    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.isPrivate(),
                post.getUser().getId()
        );
    }
}
