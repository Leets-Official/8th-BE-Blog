package com.example.demo.post.serivce;

import com.example.demo.global.apiPayload.code.GeneralErrorCode;
import com.example.demo.global.apiPayload.exception.GeneralException;
import com.example.demo.member.entity.Member;
import com.example.demo.member.repository.MemberRepository;
import com.example.demo.post.converter.PostConverter;
import com.example.demo.post.dto.PostReqDTO;
import com.example.demo.post.dto.PostResDTO;
import com.example.demo.post.entity.Post;
import com.example.demo.post.exception.PostErrorCode;
import com.example.demo.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.demo.global.apiPayload.code.GeneralErrorCode.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostConverter postConverter;

    @Transactional
    public PostResDTO.PostCreateResponse createPost(PostReqDTO.PostCreateRequest request) {

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new GeneralException(PostErrorCode.MEMBER_NOT_FOUND));


        Post post = Post.builder()
                .member(member)
                .title(request.title())
                .content(request.content())
                .build();

        Post savedPost = postRepository.save(post);

        return postConverter.toPostCreateResponse(savedPost);
    }

    public PostResDTO.PostGetResponse getPost(Long postId){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(PostErrorCode.POST_NOT_FOUND));

        return postConverter.toPostGetResponse(post);
    }

    @Transactional
    public PostResDTO.PostUpdateResponse updatePost(Long postId,PostReqDTO.PostUpdateRequest request){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(PostErrorCode.POST_NOT_FOUND));

        if (!post.getMember().getId().equals(request.memberId())) {
            throw new GeneralException(PostErrorCode.POST_FORBIDDEN);
        }

        post.update(request.title(), request.content());


        return postConverter.toPostUpdateResponse(post);
    }

    @Transactional
    public PostResDTO.PostDeleteResponse deletePost(Long postId,PostReqDTO.PostDeletePost request){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(PostErrorCode.POST_NOT_FOUND));

        if (!post.getMember().getId().equals(request.memberId())) {
            throw new GeneralException(PostErrorCode.POST_FORBIDDEN);
        }
        post.delete();

        return postConverter.toPostDeleteResponse(postId);
    }

    public PostResDTO.PostGetListResponse getPostList(){
        List<Post> posts = postRepository.findAll();

        return postConverter.toPostGetListResponse(posts);
    }
}
