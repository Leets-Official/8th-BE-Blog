package com.leets_8th_be.kanghyeonwoo.blog.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalPostUseCases implements CreatePostUseCase, GetPostUseCase {
    private final CreatePostUseCase createPost;
    private final GetPostUseCase getPost;
    private final TransactionTemplate writeTransaction;
    private final TransactionTemplate readTransaction;

    public TransactionalPostUseCases(CreatePostUseCase createPost, GetPostUseCase getPost,
                                     TransactionTemplate writeTransaction,
                                     TransactionTemplate readTransaction) {
        this.createPost = createPost;
        this.getPost = getPost;
        this.writeTransaction = writeTransaction;
        this.readTransaction = readTransaction;
    }

    @Override
    public PostView create(CreatePostCommand command) {
        return writeTransaction.execute(status -> createPost.create(command));
    }

    @Override
    public PostView get(PostId id) {
        return readTransaction.execute(status -> getPost.get(id));
    }
}
