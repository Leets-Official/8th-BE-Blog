package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.PostNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.UpdatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.UpdatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostProjectionPort;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Post;

public class UpdatePostService implements UpdatePostUseCase {
    private final PostStore posts;
    private final PostProjectionPort projection;

    public UpdatePostService(PostStore posts, PostProjectionPort projection) {
        this.posts = posts;
        this.projection = projection;
    }

    @Override
    public PostView update(UpdatePostCommand command) {
        Post post = posts.findForUpdate(command.postId()).orElseThrow(PostNotFound::new);
        PostView saved = posts.save(post.update(command.authorId(), command.title(), command.content()));
        projection.synchronize(saved);
        return saved;
    }
}
