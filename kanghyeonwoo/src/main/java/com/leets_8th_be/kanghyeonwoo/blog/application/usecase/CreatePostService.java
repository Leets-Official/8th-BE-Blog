package com.leets_8th_be.kanghyeonwoo.blog.application.usecase;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.AuthorNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostStore;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.PostProjectionPort;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Post;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;

public class CreatePostService implements CreatePostUseCase {
    private final UserProfileLookup profiles;
    private final PostStore posts;
    private final PostProjectionPort projection;

    public CreatePostService(UserProfileLookup profiles, PostStore posts, PostProjectionPort projection) {
        this.profiles = profiles;
        this.posts = posts;
        this.projection = projection;
    }

    @Override
    public PostView create(CreatePostCommand command) {
        if (profiles.findById(command.authorId()).isEmpty()) {
            throw new AuthorNotFound();
        }

        PostView saved = posts.save(Post.create(command.title(), command.content(), command.authorId()));
        projection.synchronize(saved);
        return saved;
    }
}
