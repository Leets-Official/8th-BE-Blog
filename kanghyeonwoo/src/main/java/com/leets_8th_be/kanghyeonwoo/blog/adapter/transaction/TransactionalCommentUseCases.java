package com.leets_8th_be.kanghyeonwoo.blog.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentPage;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CommentSearchQuery;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetCommentsUseCase;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalCommentUseCases implements GetCommentsUseCase {
    private final GetCommentsUseCase getComments;
    private final TransactionTemplate transaction;

    public TransactionalCommentUseCases(GetCommentsUseCase getComments, TransactionTemplate transaction) {
        this.getComments = getComments;
        this.transaction = transaction;
    }

    @Override
    public CommentPage getComments(CommentSearchQuery query) {
        return transaction.execute(status -> getComments.getComments(query));
    }
}
