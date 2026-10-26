package com.leets_8th_be.kanghyeonwoo.blog.config;

import com.leets_8th_be.kanghyeonwoo.blog.adapter.transaction.TransactionalCommentUseCases;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.GetCommentsService;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.CreateCommentService;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.DeleteCommentService;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class CommentModuleConfiguration {
    @Bean
    TransactionalCommentUseCases commentUseCases(GetPostUseCase posts, UserProfileLookup users, CommentStore comments,
                                                 PlatformTransactionManager transactionManager) {
        TransactionTemplate readTransaction = new TransactionTemplate(transactionManager);
        readTransaction.setReadOnly(true);
        return new TransactionalCommentUseCases(new GetCommentsService(posts, comments), comments,
                new CreateCommentService(users, posts, comments), new DeleteCommentService(comments), readTransaction,
                new TransactionTemplate(transactionManager));
    }
}
