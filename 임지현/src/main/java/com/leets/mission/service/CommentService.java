package com.leets.mission.service;

import com.leets.mission.domain.comment.Comment;
import com.leets.mission.domain.comment.CommentRepository;
import com.leets.mission.domain.comment.CommentStatus;
import com.leets.mission.domain.member.Member;
import com.leets.mission.domain.member.MemberRepository;
import com.leets.mission.domain.post.Post;
import com.leets.mission.domain.post.PostRepository;
import com.leets.mission.domain.post.PostStatus;
import com.leets.mission.dto.comment.CommentCreateRequest;
import com.leets.mission.dto.comment.CommentResponse;
import com.leets.mission.global.exception.CustomException;
import com.leets.mission.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public List<CommentResponse> getComments(Long postId) {
        findActivePost(postId);

        return commentRepository
                .findAllByPostIdAndStatusAndDeletedAtIsNullOrderByCreatedAtAsc(
                        postId,
                        CommentStatus.ACTIVE
                )
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    public CommentResponse getComment(Long postId, Long commentId) {
        findActivePost(postId);

        Comment comment = commentRepository
                .findByIdAndPostIdAndStatusAndDeletedAtIsNull(
                        commentId,
                        postId,
                        CommentStatus.ACTIVE
                )
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        return CommentResponse.from(comment);
    }

    @Transactional
    public CommentResponse createComment(
            Long postId,
            CommentCreateRequest request,
            Long memberId
    ) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        Post post = findActivePost(postId);

        Comment comment = Comment.builder()
                .member(member)
                .post(post)
                .content(request.content())
                .build();

        return CommentResponse.from(commentRepository.save(comment));
    }

    private Post findActivePost(Long postId) {
        return postRepository
                .findByIdAndStatusAndDeletedAtIsNull(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
}