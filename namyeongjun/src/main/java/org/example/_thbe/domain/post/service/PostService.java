package org.example._thbe.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.member.entity.Member;
import org.example._thbe.domain.member.error.MemberErrorCode;
import org.example._thbe.domain.member.repository.MemberRepository;
import org.example._thbe.domain.post.dto.*;
import org.example._thbe.domain.post.entity.Post;
import org.example._thbe.domain.post.error.PostErrorCode;
import org.example._thbe.domain.post.repository.PostRepository;
import org.example._thbe.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 게시글 조회
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findWithMemberById(postId)
                .orElseThrow(()-> new CustomException(PostErrorCode.POST_NOT_FOUND));

        return PostResponse.from(post);
    }


    // 게시글 목록 조회
    public PostListResponse getPostList() {
        List<Post> postList = postRepository.findAllWithMember();
        return PostListResponse.from(postList);
    }

    // 게시글 생성
    @Transactional
    public PostCreateResponse createPost(PostCreateRequest request,Long memberId) {
        Member member = findMember(memberId);
        Post post = postRepository.save(Post.create(request.title(), request.content(),member));

        return PostCreateResponse.from(post);

    }

    // 게시글 수정
    @Transactional
    public PostUpdateResponse updatePost(PostUpdateRequest request,Long postId,Long memberId) {
        Member member = findMember(memberId);
        Post post = findPost(postId);

        if(!member.getId().equals(post.getMember().getId())) {
            throw new CustomException(PostErrorCode.NOT_MY_POST);
        }

        post.update(request.title(), request.content());

        return PostUpdateResponse.from(post);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId,Long memberId) {
        Member member = findMember(memberId);
        Post post = findPost(postId);

        if(!member.getId().equals(post.getMember().getId())) {
            throw new CustomException(PostErrorCode.NOT_MY_POST);
        }
        postRepository.delete(post);
    }



    private Member findMember(Long memberId) {
        return memberRepository
                .findById(memberId).orElseThrow(()-> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private Post findPost(Long postId) {
        return postRepository
                .findById(postId).orElseThrow(()-> new CustomException(PostErrorCode.POST_NOT_FOUND));
    }
}
