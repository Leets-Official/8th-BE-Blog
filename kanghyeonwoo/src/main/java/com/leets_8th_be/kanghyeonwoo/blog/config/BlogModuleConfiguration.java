package com.leets_8th_be.kanghyeonwoo.blog.config;

import com.leets_8th_be.kanghyeonwoo.blog.adapter.transaction.TransactionalPostUseCases;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostProjectionPort;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.CreatePostService;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.GetPostService;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.UpdatePostService;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.DeletePostService;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.HidePostService;
import com.leets_8th_be.kanghyeonwoo.blog.application.usecase.InspectPostService;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostState;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class BlogModuleConfiguration {
    @Bean
    @Order(90)
    public ApplicationRunner synchronizeExistingPostProjections(PostStore posts, PostProjectionPort projection) {
        return args -> posts.findAll().forEach(post -> {
            if (post.state() == PostState.DELETED) {
                projection.remove(post.id());
            } else {
                projection.synchronize(post);
            }
        });
    }

    @Bean
    public TransactionalPostUseCases postUseCases(UserProfileLookup userProfiles, PostStore posts,
                                                  PostProjectionPort projection,
                                                  PlatformTransactionManager transactionManager) {
        TransactionTemplate writeTransaction = new TransactionTemplate(transactionManager);
        TransactionTemplate readTransaction = new TransactionTemplate(transactionManager);
        readTransaction.setReadOnly(true);

        return new TransactionalPostUseCases(new CreatePostService(userProfiles, posts, projection),
                new GetPostService(posts), new UpdatePostService(posts, projection), new DeletePostService(posts, projection),
                new HidePostService(posts, projection), new InspectPostService(posts), writeTransaction, readTransaction);
    }
}
