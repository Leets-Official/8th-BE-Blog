package com.leets.blog.domain.comment.service;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.comment.entity.Comment;
import com.leets.blog.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 여러 서비스가 함께 쓰는 댓글 조회 (호출한 서비스의 트랜잭션 안에서 실행)
@Component
@RequiredArgsConstructor
public class CommentReader {

    private final CommentRepository commentRepository;

    // 0 이하 ID는 400, 없거나 삭제된 댓글·숨김 글의 댓글은 404
    // (글이 삭제되면 댓글도 함께 삭제되므로 부모 글의 삭제 여부는 따로 확인하지 않음)
    public Comment getVisibleComment(Long commentId) {
        if (commentId < 1) {
            throw new CustomException(ErrorCode.INVALID_COMMENT_ID);
        }
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        if (comment.getPost().isHidden()) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        return comment;
    }
}
