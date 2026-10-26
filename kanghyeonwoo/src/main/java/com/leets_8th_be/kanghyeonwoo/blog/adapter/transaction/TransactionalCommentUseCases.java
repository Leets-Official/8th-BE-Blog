package com.leets_8th_be.kanghyeonwoo.blog.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentPage;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CommentSearchQuery;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentsUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.GetCommentTargetService;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalCommentUseCases implements GetCommentsUseCase, GetCommentTargetUseCase {
    private final GetCommentsUseCase getComments;
    private final GetCommentTargetUseCase getCommentTarget;
    private final TransactionTemplate transaction;

    public TransactionalCommentUseCases(GetCommentsUseCase getComments, CommentStore comments,
                                        GetPostUseCase posts, TransactionTemplate transaction) {
        this.getComments = getComments;
        this.getCommentTarget = new GetCommentTargetService(comments, posts);
        this.transaction = transaction;
    }

    @Override
    public CommentPage getComments(CommentSearchQuery query) {
        return transaction.execute(status -> getComments.getComments(query));
    }

    @Override
    public com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView getTarget(CommentId id) {
        return transaction.execute(status -> getCommentTarget.getTarget(id));
    }
}
