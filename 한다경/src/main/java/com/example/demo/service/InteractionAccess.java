package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.exception.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InteractionAccess {
    private final MemberRepository members;
    private final PostRepository posts;
    private final CommentRepository comments;

    // Service transactions serialize requests made by the same member.
    public Member memberForUpdate(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return members.findActiveForUpdate(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Post readablePost(Long id, Long memberId) {
        validId(id);
        Post post = posts.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        checkPost(post, memberId);
        return post;
    }

    public Comment readableComment(Long id, Long memberId) {
        validId(id);
        Comment comment = comments.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        if (comment.isDeleted()) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (comment.isHidden()) {
            throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
        }
        checkPost(comment.getPost(), memberId);
        return comment;
    }

    private void checkPost(Post post, Long memberId) {
        if (post.isDeleted()) {
            throw new BusinessException(ErrorCode.POST_DELETED);
        }
        if (post.isHidden() || (post.isPrivate() && !post.getMember().getId().equals(memberId))) {
            throw new BusinessException(ErrorCode.POST_ACCESS_DENIED);
        }
    }

    public void validId(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
