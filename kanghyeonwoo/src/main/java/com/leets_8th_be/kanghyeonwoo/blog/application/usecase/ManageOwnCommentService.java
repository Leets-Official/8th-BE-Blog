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

    public ManageOwnCommentService(CommentStore c) {
        comments = c;
    }

    private Comment own(CommentId id, UserId actor) {
        var c = comments.findForUpdate(id).orElseThrow(CommentNotFound::new);
        if (!c.authorId().equals(actor)) throw new PostAccessDenied();
        if (c.deleted()) throw new CommentNotFound();
        return c;
    }

    public CommentView update(CommentId id, UserId actor, String content) {
        var c = own(id, actor);
        return comments.save(new Comment(c.id(), c.authorId(), c.parentId(), content, c.state(), c.deleted()));
    }

    public CommentView deleteOwn(CommentId id, UserId actor) {
        return comments.save(own(id, actor).delete());
    }
}
