package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.AuthorNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostProjectionPort;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Post;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.AuthorDirectory;

public class CreatePostService implements CreatePostUseCase {
    private final AuthorDirectory authors;
    private final PostStore posts;
    private final PostProjectionPort projection;

    public CreatePostService(AuthorDirectory authors, PostStore posts, PostProjectionPort projection) {
        this.authors = authors;
        this.posts = posts;
        this.projection = projection;
    }

    @Override
    public PostView create(CreatePostCommand command) {
        if (!authors.exists(command.authorId())) {
            throw new AuthorNotFound();
        }

        PostView saved = posts.save(Post.create(command.title(), command.content(), command.authorId()));
        projection.synchronize(saved);
        return saved;
    }
}
