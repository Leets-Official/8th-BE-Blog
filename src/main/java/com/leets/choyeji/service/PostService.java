package com.leets.choyeji.service;

import com.leets.choyeji.dto.PostListResponse;
import com.leets.choyeji.dto.PostRequest;
import com.leets.choyeji.dto.PostResponse;
import com.leets.choyeji.dto.PostUpdateResponse;
import com.leets.choyeji.entity.Member;
import com.leets.choyeji.entity.Post;
import com.leets.choyeji.exception.ApiException;
import com.leets.choyeji.repository.CommentRepository;
import com.leets.choyeji.repository.MemberRepository;
import com.leets.choyeji.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    public PostResponse get(Long postId) {
        return PostResponse.from(findPost(postId));
    }

    public PostListResponse list(int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)),
                Sort.by(Sort.Direction.DESC, "id"));
        return PostListResponse.from(postRepository.findAll(pageable).map(PostResponse::from));
    }

    @Transactional
    // 인증 복원 시: public PostResponse create(Long memberId, PostRequest request)
    public PostResponse create(PostRequest request) {
        // Member member = findMember(memberId);
        Member member = findOrCreateAnonymousMember();
        Post post = postRepository.save(new Post(member, request.title(), request.content()));
        return PostResponse.from(post);
    }

    @Transactional
    // 인증 복원 시: public PostUpdateResponse update(Long memberId, Long postId, PostRequest request)
    public PostUpdateResponse update(Long postId, PostRequest request) {
        // findMember(memberId);
        Post post = findPost(postId);
        // checkOwner(post, memberId);
        post.update(request.title(), request.content());
        return PostUpdateResponse.from(post);
    }

    @Transactional
    // 인증 복원 시: public void delete(Long memberId, Long postId)
    public void delete(Long postId) {
        // findMember(memberId);
        Post post = findPost(postId);
        // checkOwner(post, memberId);
        commentRepository.deleteAllByPostId(postId);
        postRepository.delete(post);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(ApiException::postNotFound);
    }

    private Member findOrCreateAnonymousMember() {
        // Post.member가 필수이므로 로그인 비활성화 중에는 공용 익명 작성자를 사용합니다.
        return memberRepository.findFirstByEmailOrderByIdAsc("anonymous@local.invalid")
                .orElseGet(() -> memberRepository.save(
                        new Member("!LOGIN_DISABLED!", "익명", "anonymous@local.invalid")));
    }

    /* 로그인 및 작성자 권한 검사: 로그인 기능 연결 시 복원합니다.
    private Member findMember(Long memberId) {
        if (memberId == null) {
            throw ApiException.unauthorized();
        }
        return memberRepository.findById(memberId).orElseThrow(ApiException::unauthorized);
    }

    private void checkOwner(Post post, Long memberId) {
        if (!post.getMember().getId().equals(memberId)) {
            throw ApiException.forbidden();
        }
    }
    */
}
