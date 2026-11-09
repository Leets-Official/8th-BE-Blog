package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.CommentNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;

public class GetCommentTargetService implements GetCommentTargetUseCase {
    private final CommentStore comments;
    public GetCommentTargetService(CommentStore comments) {
        this.comments = comments;
    }

    @Override
    public CommentView getTarget(CommentId id) {
        return comments.findTarget(id).orElseThrow(CommentNotFound::new);
    }
}
