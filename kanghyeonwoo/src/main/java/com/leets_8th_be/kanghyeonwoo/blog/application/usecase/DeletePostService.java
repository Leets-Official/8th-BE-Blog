package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.PostNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.DeletePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.DeletePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostProjectionPort;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Post;

public class DeletePostService implements DeletePostUseCase {
    private final PostStore posts;
    private final PostProjectionPort projection;

    public DeletePostService(PostStore posts, PostProjectionPort projection) {
        this.posts = posts;
        this.projection = projection;
    }

    @Override
    public com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId delete(DeletePostCommand command) {
        Post post = posts.findForUpdate(command.postId()).orElseThrow(PostNotFound::new);
        post.requireAuthor(command.authorId());
        posts.delete(post.id());
        projection.remove(post.id());
        return post.id();
    }
}
