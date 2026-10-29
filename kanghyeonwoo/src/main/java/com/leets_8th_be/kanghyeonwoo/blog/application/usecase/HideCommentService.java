package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.CommentNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HideCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;

public class HideCommentService implements HideCommentUseCase {
    private final CommentStore comments;

    public HideCommentService(CommentStore comments) {
        this.comments = comments;
    }

    @Override
    public com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView hide(CommentId id) {
        var comment = comments.findForUpdate(id).orElseThrow(CommentNotFound::new).comment();
        if (comment.deleted()) throw new CommentNotFound();
        return comments.save(comment.hide());
    }
}
