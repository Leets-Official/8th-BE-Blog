package com.leets.mission.domain.report.handler;

import com.leets.mission.domain.comment.entity.Comment;
import com.leets.mission.domain.comment.repository.CommentRepository;
import com.leets.mission.domain.report.entity.ReportTargetType;
import com.leets.mission.global.apiPayload.code.ErrorStatus;
import com.leets.mission.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 댓글 신고 대상 처리
@Component
@RequiredArgsConstructor
public class CommentReportTargetHandler implements ReportTargetHandler {

    private final CommentRepository commentRepository;

    @Override
    public ReportTargetType getTargetType() {
        return ReportTargetType.COMMENT;
    }

    // 숨김 댓글, 숨김 게시글의 댓글은 없는 댓글과 같이 취급 (404)
    @Override
    public Long getActiveAuthorId(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> !c.isHidden() && !c.getPost().isHidden())
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_404_1));
        return comment.getUser().getId();
    }

    @Override
    public void hide(Long commentId) {
        commentRepository.findById(commentId)
                .filter(c -> !c.isHidden())
                .ifPresent(Comment::hide);
    }
}
