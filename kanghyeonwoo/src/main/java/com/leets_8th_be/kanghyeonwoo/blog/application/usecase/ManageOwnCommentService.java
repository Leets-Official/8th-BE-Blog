package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.CommentNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.ManageOwnCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Comment;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostAccessDenied;
import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

public class ManageOwnCommentService implements ManageOwnCommentUseCase {
    private final CommentStore comments;

    public ManageOwnCommentService(CommentStore comments) {
        this.comments = comments;
    }

    private Comment requireOwnedComment(CommentId id, UserId actor) {
        var comment = comments.findForUpdate(id).orElseThrow(CommentNotFound::new);
        if (!comment.authorId().equals(actor)) {
            throw new PostAccessDenied();
        }
        if (comment.deleted()) {
            throw new CommentNotFound();
        }
        return comment;
    }

    public CommentView update(CommentId id, UserId actor, String content) {
        var comment = requireOwnedComment(id, actor);
        Comment updated = new Comment(
                comment.id(), comment.authorId(), comment.parentId(), content, comment.state(), comment.deleted());
        return comments.save(updated);
    }

    public CommentView deleteOwn(CommentId id, UserId actor) {
        Comment comment = requireOwnedComment(id, actor);
        return comments.save(comment.delete());
    }
}
