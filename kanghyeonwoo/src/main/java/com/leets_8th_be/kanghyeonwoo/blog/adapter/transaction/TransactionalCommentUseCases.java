package com.leets_8th_be.kanghyeonwoo.blog.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentPage;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CommentSearchQuery;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentsUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.GetCommentTargetService;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HideCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.InspectCommentTargetUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreateCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreateCommentCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.DeleteCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.HideCommentService;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalCommentUseCases implements GetCommentsUseCase, GetCommentTargetUseCase,
        InspectCommentTargetUseCase, HideCommentUseCase, CreateCommentUseCase, DeleteCommentUseCase {
    private final GetCommentsUseCase getComments;
    private final GetCommentTargetUseCase getCommentTarget;
    private final HideCommentUseCase hideComment;
    private final CreateCommentUseCase createComment;
    private final DeleteCommentUseCase deleteComment;
    private final CommentStore comments;
    private final TransactionTemplate transaction;
    private final TransactionTemplate writeTransaction;

    public TransactionalCommentUseCases(GetCommentsUseCase getComments, CommentStore comments,
                                        CreateCommentUseCase createComment, DeleteCommentUseCase deleteComment,
                                        TransactionTemplate transaction, TransactionTemplate writeTransaction) {
        this.getComments = getComments;
        this.comments = comments;
        this.getCommentTarget = new GetCommentTargetService(comments);
        this.hideComment = new HideCommentService(comments);
        this.createComment = createComment;
        this.deleteComment = deleteComment;
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

    @Override
    public com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView create(CreateCommentCommand command) {
        return writeTransaction.execute(status -> createComment.create(command));
    }

    @Override
    public com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView delete(CommentId id) {
        return writeTransaction.execute(status -> deleteComment.delete(id));
    }
}
