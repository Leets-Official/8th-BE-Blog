package com.example.demo.service;

import com.example.demo.dto.post.*;
import com.example.demo.entity.Member;
import com.example.demo.entity.Post;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    // US-1 목록 조회
    public List<PostSummaryResponse> getPosts(int page, int size) {
        if (page < 0 || size < 1) {
            throw new CustomException(ErrorCode.INVALID_PAGE);
        }
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findAll(pageRequest).stream()
                .map(PostSummaryResponse::from)
                .toList();
    }

    // US-2 상세 조회
    public PostDetailResponse getPost(Long postId) {
        return PostDetailResponse.from(findPost(postId));
    }

    // US-3 작성
    @Transactional
    public PostCreateResponse createPost(Long memberId, PostCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .member(member)
                .build();

        return PostCreateResponse.from(postRepository.save(post));
    }

    // US-4 수정
    @Transactional
    public PostDetailResponse updatePost(Long postId, Long memberId, PostUpdateRequest request) {
        if (request.title() != null && request.title().isBlank()) {
            throw new CustomException(ErrorCode.POST_TITLE_EMPTY);
        }
        if (request.content() != null && request.content().isBlank()) {
            throw new CustomException(ErrorCode.POST_CONTENT_EMPTY);
        }

        Post post = findPost(postId);
        checkWriter(post, memberId);

        post.update(request.title(), request.content());
        postRepository.flush(); // 수정 시각(updatedAt)을 응답에 바로 담기 위해 지금 DB에 반영
        return PostDetailResponse.from(post);
    }

    // US-5 삭제
    @Transactional
    public void deletePost(Long postId, Long memberId) {
        Post post = findPost(postId);
        checkWriter(post, memberId);
        postRepository.delete(post);
    }

    // US-6 공통: 없으면 POST4041
    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    // 작성자 본인이 아니면 POST4031
    private void checkWriter(Post post, Long memberId) {
        if (!post.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.POST_FORBIDDEN);
        }
    }
}