package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.CommentNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.DeleteCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;

public class DeleteCommentService implements DeleteCommentUseCase {
    private final CommentStore comments;
    public DeleteCommentService(CommentStore comments) { this.comments = comments; }
    @Override public CommentView delete(CommentId id) {
        var comment = comments.findForUpdate(id).orElseThrow(CommentNotFound::new).comment();
        return comments.save(comment.delete());
    }
}
