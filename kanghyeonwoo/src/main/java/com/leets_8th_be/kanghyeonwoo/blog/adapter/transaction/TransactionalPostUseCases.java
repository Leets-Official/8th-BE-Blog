package com.leets_8th_be.kanghyeonwoo.blog.adapter.transaction;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.GetPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.UpdatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.UpdatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.DeletePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.DeletePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HidePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.InspectPostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalPostUseCases implements CreatePostUseCase, GetPostUseCase, InspectPostUseCase,
        UpdatePostUseCase, DeletePostUseCase, HidePostUseCase {
    private final CreatePostUseCase createPost;
    private final GetPostUseCase getPost;
    private final InspectPostUseCase inspectPost;
    private final UpdatePostUseCase updatePost;
    private final DeletePostUseCase deletePost;
    private final HidePostUseCase hidePost;
    private final TransactionTemplate writeTransaction;
    private final TransactionTemplate readTransaction;

    public TransactionalPostUseCases(CreatePostUseCase createPost, GetPostUseCase getPost,
                                     UpdatePostUseCase updatePost, DeletePostUseCase deletePost,
                                     HidePostUseCase hidePost,
                                     InspectPostUseCase inspectPost,
                                     TransactionTemplate writeTransaction,
                                     TransactionTemplate readTransaction) {
        this.createPost = createPost;
        this.getPost = getPost;
        this.inspectPost = inspectPost;
        this.updatePost = updatePost;
        this.deletePost = deletePost;
        this.hidePost = hidePost;
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

    @Override
    public java.util.Optional<PostView> inspect(PostId id) {
        return readTransaction.execute(status -> inspectPost.inspect(id));
    }

    @Override
    public PostView update(UpdatePostCommand command) {
        return writeTransaction.execute(status -> updatePost.update(command));
    }

    @Override
    public PostId delete(DeletePostCommand command) {
        return writeTransaction.execute(status -> deletePost.delete(command));
    }

    @Override
    public PostView hide(PostId id) {
        return writeTransaction.execute(status -> hidePost.hide(id));
    }
}
