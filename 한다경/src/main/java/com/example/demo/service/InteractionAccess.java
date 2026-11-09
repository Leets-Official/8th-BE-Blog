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
        requireId(id, ErrorCode.MEMBER_NOT_FOUND);
        return members.findActiveForUpdate(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    Member activeMember(Long id) {
        requireId(id, ErrorCode.MEMBER_NOT_FOUND);
        return members.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Post readablePost(Long id, Long memberId) {
        return readablePost(id, memberId, ErrorCode.INVALID_INPUT);
    }

    Post readablePost(Long id, Long memberId, ErrorCode invalidIdError) {
        Post post = findPost(id, invalidIdError);
        requireReadable(post, memberId);
        return post;
    }

    Post ownedPost(Long id, Long memberId) {
        Post post = findPost(id, ErrorCode.INVALID_POST_ID);
        requireActive(post);
        if (!post.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.POST_ACCESS_DENIED);
        }
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
        requireReadable(comment.getPost(), memberId);
        return comment;
    }

    private Post findPost(Long id, ErrorCode invalidIdError) {
        requireId(id, invalidIdError);
        return posts.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private void requireReadable(Post post, Long memberId) {
        requireActive(post);
        if (post.isHidden() || (post.isPrivate() && !post.isOwnedBy(memberId))) {
            throw new BusinessException(ErrorCode.POST_ACCESS_DENIED);
        }
    }

    private void requireActive(Post post) {
        if (post.isDeleted()) {
            throw new BusinessException(ErrorCode.POST_DELETED);
        }
    }

    public void validId(Long id) {
        requireId(id, ErrorCode.INVALID_INPUT);
    }

    private void requireId(Long id, ErrorCode invalidIdError) {
        if (id == null || id <= 0) {
            throw new BusinessException(invalidIdError);
        }
    }
}
