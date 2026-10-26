package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.PostNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.UpdatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.UpdatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Post;

public class UpdatePostService implements UpdatePostUseCase {
    private final PostStore posts;

    public UpdatePostService(PostStore posts) {
        this.posts = posts;
    }

    @Override
    public PostView update(UpdatePostCommand command) {
        Post post = posts.findForUpdate(command.postId()).orElseThrow(PostNotFound::new);
        return posts.save(post.update(command.authorId(), command.title(), command.content()));
    }
}
