package com.leets_8th_be.kanghyeonwoo.blog.config;

import com.leets_8th_be.kanghyeonwoo.blog.adapter.transaction.TransactionalPostUseCases;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.CreatePostService;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.GetPostService;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.UpdatePostService;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.DeletePostService;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.HidePostService;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.InspectPostService;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class BlogModuleConfiguration {
    @Bean
    public TransactionalPostUseCases postUseCases(UserProfileLookup userProfiles, PostStore posts,
                                                  PlatformTransactionManager transactionManager) {
        TransactionTemplate writeTransaction = new TransactionTemplate(transactionManager);
        TransactionTemplate readTransaction = new TransactionTemplate(transactionManager);
        readTransaction.setReadOnly(true);

        return new TransactionalPostUseCases(new CreatePostService(userProfiles, posts),
                new GetPostService(posts), new UpdatePostService(posts), new DeletePostService(posts),
                new HidePostService(posts), new InspectPostService(posts), writeTransaction, readTransaction);
    }
}
