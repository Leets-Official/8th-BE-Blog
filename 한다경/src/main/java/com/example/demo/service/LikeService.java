package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.dto.response.LikeResponse;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {
    private final InteractionAccess access;
    private final PostLikeRepository postLikes;
    private final CommentLikeRepository commentLikes;

    public LikeResponse likePost(Long postId, Long userId) {
        Member member = access.memberForUpdate(userId);
        Post post = access.readablePost(postId, userId);
        if (!postLikes.existsByMemberIdAndPostId(userId, postId)) {
            postLikes.saveAndFlush(new PostLike(member, post));
        }
        return new LikeResponse(postLikes.countByPostId(postId), true);
    }

    public void unlikePost(Long postId, Long userId) {
        access.memberForUpdate(userId);
        access.validId(postId);
        postLikes.deleteByMemberIdAndPostId(userId, postId);
    }

    public LikeResponse likeComment(Long commentId, Long userId) {
        Member member = access.memberForUpdate(userId);
        Comment comment = access.readableComment(commentId, userId);
        if (!commentLikes.existsByMemberIdAndCommentId(userId, commentId)) {
            commentLikes.saveAndFlush(new CommentLike(member, comment));
        }
        return new LikeResponse(commentLikes.countByCommentId(commentId), true);
    }

    public void unlikeComment(Long commentId, Long userId) {
        access.memberForUpdate(userId);
        access.validId(commentId);
        commentLikes.deleteByMemberIdAndCommentId(userId, commentId);
    }
}
