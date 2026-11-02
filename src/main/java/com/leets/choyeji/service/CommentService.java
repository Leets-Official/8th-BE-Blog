package com.leets.choyeji.service;

import com.leets.choyeji.dto.CommentRequest;
import com.leets.choyeji.dto.CommentResponse;
import com.leets.choyeji.entity.Comment;
import com.leets.choyeji.entity.Member;
import com.leets.choyeji.entity.Post;
import com.leets.choyeji.entity.PostStatus;
import com.leets.choyeji.exception.ApiException;
import com.leets.choyeji.repository.CommentRepository;
import com.leets.choyeji.repository.MemberRepository;
import com.leets.choyeji.repository.PostRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CommentResponse create(Long memberId, Long postId, CommentRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(ApiException::unauthorized);
        Post post = postRepository.findByIdForUpdate(postId).orElseThrow(ApiException::postNotFound);
        post.requireActive();
        Comment comment = commentRepository.save(new Comment(post, member, request.content()));
        return CommentResponse.from(comment);
    }

    public List<CommentResponse> list(Long postId) {
        postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE).orElseThrow(ApiException::postNotFound);
        return commentRepository.findAllByPostIdOrderByIdAsc(postId).stream()
                .map(CommentResponse::from).toList();
    }
}
