package com.leets_8th_be.kanghyeonwoo.blog.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentPage;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CommentSearchQuery;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentsUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.GetCommentTargetService;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HideCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.InspectCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.HideCommentService;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalCommentUseCases implements GetCommentsUseCase, GetCommentTargetUseCase,
        InspectCommentTargetUseCase, HideCommentUseCase {
    private final GetCommentsUseCase getComments;
    private final GetCommentTargetUseCase getCommentTarget;
    private final HideCommentUseCase hideComment;
    private final CommentStore comments;
    private final TransactionTemplate transaction;
    private final TransactionTemplate writeTransaction;

    public TransactionalCommentUseCases(GetCommentsUseCase getComments, CommentStore comments,
                                        TransactionTemplate transaction, TransactionTemplate writeTransaction) {
        this.getComments = getComments;
        this.comments = comments;
        this.getCommentTarget = new GetCommentTargetService(comments);
        this.hideComment = new HideCommentService(comments);
        this.transaction = transaction;
        this.writeTransaction = writeTransaction;
    }

    @Override
    public CommentPage getComments(CommentSearchQuery query) {
        return transaction.execute(status -> getComments.getComments(query));
    }

    @Override
    public com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView getTarget(CommentId id) {
        return transaction.execute(status -> getCommentTarget.getTarget(id));
    }

    @Override
    public java.util.Optional<com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView> inspect(CommentId id) {
        return transaction.execute(status -> comments.findTarget(id));
    }

    @Override
    public com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView hide(CommentId id) {
        return writeTransaction.execute(status -> hideComment.hide(id));
    }
}
